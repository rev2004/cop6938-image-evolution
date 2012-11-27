package imageEvolveWeb;

import imageEvolveCore.EvoControl;
import imageEvolveCore.ImgEvolution;
import imageEvolveCore.EvoControl.Evolution;

import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread;

import javax.imageio.ImageIO;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.UpdateCondition;

public class EvolutionManager implements Runnable{


	/** SimpleDB connection;
	 * Thread local used to prevent blocking when threads concurrent.
	 */
	private static final ThreadLocal<AmazonSimpleDBClient> sdb = new ThreadLocal<AmazonSimpleDBClient>() {
		@Override protected AmazonSimpleDBClient initialValue() { 
			try {
				AWSCredentials cred = new PropertiesCredentials(
						EvoRequestServlet.class.getClassLoader()
						.getResourceAsStream("AwsCredentials.properties"));
				return new AmazonSimpleDBClient(cred);
			} catch (IOException e) {
				return null;
			}
		}
	};

	private static class EvoJob{
		public Thread thread;
		public ImgEvolution evo;
		public EvoJob(){
			thread = null;
			evo = null;
		}
	}

	private EvoJob[] jobs;


	public EvolutionManager(){
		jobs = new EvoJob[2];
	}
	public EvolutionManager(int maxJobs){
		jobs = new EvoJob[maxJobs];
	}

	@Override
	public void run() {
		while(true){
			for (int i=0; i<jobs.length; i++){
				// if empty, get a new job off the queue
				if (jobs[i]==null){
					// get job and run
					jobs[i] = getQueueJob();
					if (jobs[i]!=null){
						jobs[i].thread.start();
					}	
				}
				// if terminated, store result get new job
				else if(!jobs[i].thread.isAlive()){
					// store result of job
					storeJobResult(jobs[i].evo);
					// get new job and run
					jobs[i] = getQueueJob();
					if (jobs[i]!=null){
						jobs[i].thread.start();
					}
				}
				// sleep for a minute before trying another
				try { Thread.sleep(60000); } catch (InterruptedException ex) {}
			}
		}
	}

	private boolean storeJobResult(ImgEvolution evo){
		// output generated image to S3
		boolean done = !StorageManagement.storeImage(evo.outImg+"_r", evo.getBestImg());
		// write result back to SimpleDB
		while (!done){
			// create base request
			PutAttributesRequest putReq = new PutAttributesRequest();
			putReq = putReq.withDomainName("ImgEvo_images").withItemName(evo.outImg);
			putReq = putReq.withAttributes(new ReplaceableAttribute("fitness", 
					Double.toString(evo.getBest().getFitness()),true));
			// get current values
			GetAttributesResult existing = sdb.get().getAttributes(new GetAttributesRequest()
				.withDomainName("ImgEvo_images").withItemName(evo.outImg)
				.withAttributeNames("fitness")
				.withConsistentRead(true));
			// set update condition to prevent overwriting existing image
			putReq = putReq.withExpected(new UpdateCondition().withName("fitness").withExists(false));
			for(Attribute a : existing.getAttributes()){
				if(a.getName().equals("fitness")){
					// if current fitness greater, overwrite (conditionally)
					if (evo.getBest().getFitness() > Double.parseDouble(a.getValue())){
						putReq = putReq.withExpected(new UpdateCondition()
							.withName("fitness")
							.withValue(a.getValue())
							.withExists(true));
					}
					// weird race conditions have happened break out and do nothing
					else {
						done = true;
					}
					
				}
			}
			// if still valid
			if (!done){
				// make update
				sdb.get().putAttributes(putReq);
			}
			
			// check update success/validity
			existing = sdb.get().getAttributes(new GetAttributesRequest()
				.withDomainName("ImgEvo_images").withItemName(evo.outImg)
				.withAttributeNames("fitness")
				.withConsistentRead(true));
			for(Attribute a : existing.getAttributes()){
				if(a.getName().equals("fitness")){
					// if current fitness greater, overwrite (conditionally)
					if (evo.getBest().getFitness() <= Double.parseDouble(a.getValue())){
						// update was successful or already bested
						done = true;
					}
				}
			}
		}
		// remove from queue
		ReqQueueManagement.delSqsMsg(evo.control.receiptHandle);
		return true;
	}


	private EvoJob getQueueJob(){
		// make temporary job object
		EvoJob tmp = new EvoJob();
		// get message from queue (may wait a long time for a queue message)
		ReqQueueManagement req = ReqQueueManagement.recvSqsMsg(900);
		// get target image
		InputStream imgS3 = StorageManagement.getImage(
				(req.targetId==null||req.targetId.length()<1)?req.imageId+"_o":req.targetId);
		// make a EvoControl and ImgEvolution
		tmp.evo = new ImgEvolution("evo");
		try {
			tmp.evo.sourceImg = ImageIO.read(imgS3);
			imgS3.close();
			tmp.evo.outImg = req.imageId;
			tmp.evo.control.size_x = tmp.evo.sourceImg.getWidth();
			tmp.evo.control.size_y = tmp.evo.sourceImg.getHeight();
			tmp.evo.control.polygons = 100;
			tmp.evo.control.vertices = 6;
			tmp.evo.control.population = 0;
			tmp.evo.control.alg = Evolution.HC;
			tmp.evo.control.initColor = EvoControl.InitColor.RAND;
			tmp.evo.control.mutationMode = EvoControl.Mutation.MEDIUM;
			tmp.evo.control.comparison = EvoControl.CompMode.DIFF;
			tmp.evo.control.mutationChance = 0.0;
			tmp.evo.control.mutationDegree = 0.0;
			tmp.evo.control.uniformCross = false;
			tmp.evo.control.parentCutoff = 0.0;
			tmp.evo.control.killParents = false;
			tmp.evo.control.rndCutoff = false;
			tmp.evo.control.threshold = req.fitThresh;
			tmp.evo.control.maxGenerations = req.genThresh;
			tmp.evo.control.receiptHandle = req.receiptHandle;
			tmp.thread = new Thread(tmp.evo);
			return tmp;
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
	}

}
