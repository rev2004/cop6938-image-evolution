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
						RequestServlet.class.getClassLoader()
						.getResourceAsStream("AwsCredentials.properties"));
				return new AmazonSimpleDBClient(cred);
			} catch (IOException e) {
				return null;
			}
		}
	};
	
	private static final boolean ALWAYS_SLEEP = true;

	protected static class EvoJob{
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
				boolean jobStarted = false;
				// if empty, get a new job off the queue
				if (jobs[i]==null){
					System.out.println("EvoMgr-run: empty slot, i="+i);
					// get job and run
					jobs[i] = getQueueJob();
					System.out.println("EvoMgr-run: gotjob, not_null="+(jobs[i]!=null));
					if (jobs[i]!=null){
						System.out.println("EvoMgr-run: start job");
						jobs[i].thread.start();
						jobStarted = true;
					}	
				}
				// if terminated, store result get new job
				else if(!jobs[i].thread.isAlive()){
					System.out.println("EvoMgr-run: finished job slot, i="+i);
					// store result of job
					storeJobResult(jobs[i].evo);
					System.out.println("EvoMgr-run: result stored");
					// get new job and run
					jobs[i] = getQueueJob();
					System.out.println("EvoMgr-run: gotjob, not_null="+(jobs[i]!=null));
					if (jobs[i]!=null){
						System.out.println("EvoMgr-run: start job");
						jobs[i].thread.start();
						jobStarted = true;
					}
				}
				// if a job was started wait before running again
				if (jobStarted || ALWAYS_SLEEP){
					System.out.println("EvoMgr-run: sleep 60 sec");
					// sleep for a minute before trying another
					try { Thread.sleep(60000); } catch (InterruptedException ex) {}
				}
			}
		}
	}

	protected static boolean storeJobResult(ImgEvolution evo){
		// output generated image to S3
		boolean done = !ImageManagement.storeImage(evo.outImg+"_r", evo.getBestImg());
		System.out.println("EvoMgr-storeJobResult: image stored done="+done);
		// write result back to SimpleDB
		while (!done){
			System.out.println("EvoMgr-storeJobResult: !done loop");
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
			System.out.println("EvoMgr-storeJobResult: got previous");
			for(Attribute a : existing.getAttributes()){
				if(a.getName().equals("fitness")){
					// if current fitness greater, overwrite (conditionally)
					if (evo.getBest().getFitness() > Double.parseDouble(a.getValue())){
						putReq = putReq.withExpected(new UpdateCondition()
							.withName("fitness")
							.withValue(a.getValue())
							.withExists(true));
						System.out.println("EvoMgr-storeJobResult: lesser previous fitness found old="+a.getValue()+" new="+Double.toString(evo.getBest().getFitness()));
					}
					// weird race conditions have happened break out and do nothing
					else {
						done = true;
						System.out.println("EvoMgr-storeJobResult: existing fitness greater old="+a.getValue()+" new="+Double.toString(evo.getBest().getFitness()));
					}
					
				}
			}
			// if still valid
			if (!done){
				System.out.println("EvoMgr-storeJobResult: !done, put update");
				// make update
				sdb.get().putAttributes(putReq);
				System.out.println("EvoMgr-storeJobResult: !done, put update done");
			}
			System.out.println("EvoMgr-storeJobResult: get existing");
			// check update success/validity
			existing = sdb.get().getAttributes(new GetAttributesRequest()
				.withDomainName("ImgEvo_images").withItemName(evo.outImg)
				.withAttributeNames("fitness")
				.withConsistentRead(true));
			System.out.println("EvoMgr-storeJobResult: got existing");
			for(Attribute a : existing.getAttributes()){
				if(a.getName().equals("fitness")){
					// if current fitness greater, overwrite (conditionally)
					System.out.println("EvoMgr-storeJobResult: fitness attribute found");
					if (evo.getBest().getFitness() <= Double.parseDouble(a.getValue())){
						System.out.println("EvoMgr-storeJobResult: existing fit >= this fitness - this="+evo.getBest().getFitness()+" existing="+Double.parseDouble(a.getValue()));
						// update was successful or already bested
						done = true;
					}
				}
			}
		}
		System.out.println("EvoMgr-storeJobResult: remove queue record");
		// remove from queue
		RequestManagement.delSqsMsg(evo.control.receiptHandle);
		System.out.println("EvoMgr-storeJobResult: return");
		return true;
	}

	protected static EvoJob getQueueJob(){
		try {
			// make temporary job object
			EvoJob tmp = new EvoJob();
			// get message from queue (may wait a long time for a queue message)
			RequestManagement req = RequestManagement.recvSqsMsg(900);
			System.out.println("EvoMgr-getQueueJob: recvd msg, msg not_null="+(req!=null));
			if(req!=null){
				// get target image
				InputStream imgS3 = ImageManagement.getImage(
						(req.targetId==null||req.targetId.equals(""))?req.imageId+"_o":req.targetId);
				// if target image not found, stop and return null
				if(imgS3==null){
					return null;
				}
				// make a EvoControl and ImgEvolution
				tmp.evo = new ImgEvolution("evo");
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
			} else {
				return null;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}
	}

}
