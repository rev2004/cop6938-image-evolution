package imageEvolveWeb;

import imageEvolveCore.EvoControl;
import imageEvolveCore.ImgEvolution;
import imageEvolveCore.EvoControl.Evolution;

import java.io.File;
import java.lang.Thread;

public class EvolutionManager implements Runnable{

	private Thread[] jobs;
	
	
	public EvolutionManager(){
		jobs = new Thread[2];
	}
	public EvolutionManager(int maxJobs){
		jobs = new Thread[maxJobs];
	}
	
	@Override
	public void run() {
		while(true){
			for (int i=0; i<jobs.length; i++){
				// if terminated get another job off the queue
				if (!jobs[i].isAlive()){
					// get message from queue (may wait a long time for a queue message)
					//ReqQueueManagement.recvSqsMsg(900);
					
					// get target image
					
					// make a EvoControl and ImgEvolution
					ImgEvolution e = new ImgEvolution("evo");
					e.sourceImg = ImgEvolution.getSourceImage(new File("canvas.png"));
					e.outImg = "";
					e.control.size_x = e.sourceImg.getWidth();
					e.control.size_y = e.sourceImg.getHeight();
					e.control.polygons = 100;
					e.control.vertices = 6;
					e.control.population = 40;
					e.control.alg = Evolution.HC;
					e.control.initColor = EvoControl.InitColor.RAND;
					e.control.mutationMode = EvoControl.Mutation.MEDIUM;
					e.control.comparison = EvoControl.CompMode.DIFF;
					e.control.mutationChance = 0.2;
					e.control.mutationDegree = 0.1;
					e.control.uniformCross = true;
					e.control.parentCutoff = 0.1;
					e.control.killParents = true;
					e.control.rndCutoff = false;
					e.control.threshold = 0.93;
					// make thread and run
					jobs[i] = new Thread(e);
					jobs[i].start();
				}
				// sleep for a minute before trying another
				try { Thread.sleep(60000); } catch (InterruptedException ex) {}
			}
		}
	}

}
