package imageEvolve;

import imageEvolve.EvoImg;
import imageEvolve.EvoControl.Evolution;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Random;
import javax.imageio.ImageIO;

/** Class is the main driver to create images evolved by a genetic programming algorithm.
 *  <br>This class was designed for for use in a cloud-based web application, for novelty 
 *  and as an experiment in building scalable cloud-based applications. Depending on the
 *  evolution method used, this program will either run a hill climbing algorithm or 
 *  a proper genetic algorithm. 
 *  
 *  <br>Original concept and some implementation ideas taken from Roger Alsing's Evolving Mona Lisa
 *  project. Other ideas and implementation reference provided by AlteredQualia and Nihilogic.
 *  @see {@link http://rogeralsing.com/2008/12/07/genetic-programming-evolution-of-mona-lisa/ }
 *  @see {@link http://blog.nihilogic.dk/2009/01/genetic-mona-lisa.html }
 *  @see {@link http://alteredqualia.com/visualization/evolve/ }
 *  
 * @author William Strickland (University of Central Florida)
 * @since 2012-10-30
 * @version 0.7
 */
public class ImgEvolution implements Runnable{
	
	/* Source of randomness.
	 * ThreadLocal used to avoid blocking when used by
	 * concurrent threads
	 */
	private static final ThreadLocal<Random> rndSrc =
			new ThreadLocal <Random> () {
				@Override protected Random initialValue() { return new Random(); }
	};
	
	/* Evolution instance variables */
	String name;
	EvoControl control;
	private BufferedImage sourceImg;
	private EvoImg best;
	private EvoImg[] population;
	private File outImg;
	private Writer log;
	
	/* Constructors */
	/** Default constructor
	 */
	public ImgEvolution(){
		this.name = this.toString();
		this.control = new EvoControl();
		this.best = null;
		this.population = null;
		this.outImg = null;
		this.log = null;
	}
	/** Constructor - sets name
	 * @param nm String value to set as name
	 */
	public ImgEvolution(String nm){
		this.name = nm;
		this.control = new EvoControl();
		this.best = null;
		this.population = null;
		this.outImg = null;
		this.log = null;
	}
	
	
	/** Driver for starting up a single instance of image evolution.
	 * @param args arguments are ignored
	 */
	public static void main(String[] args){
		// Make an instances
		ImgEvolution e1 = new ImgEvolution("HC"); // HC
		ImgEvolution e2 = new ImgEvolution("GA"); // GA
		try {
			e1.outImg = new File("best_hc.png");
			e2.outImg = new File("best_ga.png");
			e1.log = new FileWriter(new File("hc_log.txt").getAbsoluteFile());
			e2.log = new FileWriter(new File("ga_log.txt").getAbsoluteFile());
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		// Setup HC evolution parameters
		e1.sourceImg = ImgEvolution.getSourceImage(new File("canvas.png"));
		e1.control.size_x = e1.sourceImg.getWidth();
		e1.control.size_y = e1.sourceImg.getHeight();
		e1.control.polygons = 100;
		e1.control.vertices = 6;
		e1.control.population = 40;
		e1.control.alg = Evolution.HC;
		e1.control.initColor = EvoControl.InitColor.RAND;
		e1.control.mutationMode = EvoControl.Mutation.MEDIUM;
		e1.control.comparison = EvoControl.CompMode.DIFF;
		e1.control.mutationChance = 0.2;
		e1.control.mutationDegree = 0.1;
		e1.control.uniformCross = true;
		e1.control.parentCutoff = 0.1;
		e1.control.killParents = true;
		e1.control.rndCutoff = false;
		e1.control.threshold = 0.93;
		// Setup GA evolution parameters
		e2.sourceImg = ImgEvolution.getSourceImage(new File("canvas.png"));
		e2.control.size_x = e2.sourceImg.getWidth();
		e2.control.size_y = e2.sourceImg.getHeight();
		e2.control.polygons = 100;
		e2.control.vertices = 6;
		e2.control.population = 40;
		e2.control.alg = Evolution.GA;
		e2.control.initColor = EvoControl.InitColor.RAND;
		e2.control.mutationMode = EvoControl.Mutation.PROB;
		e2.control.comparison = EvoControl.CompMode.DIFF;
		e2.control.mutationChance = 0.2;
		e2.control.mutationDegree = 0.1;
		e2.control.uniformCross = true;
		e2.control.parentCutoff = 0.1;
		e2.control.killParents = true;
		e2.control.rndCutoff = false;
		e2.control.threshold = 0.93;
		// Run evolution in parallel (drag race HC vs GA)
		new Thread(e1).start();
		new Thread(e2).start();
	}
	
	
	/* Evolution methods */
	/** Generic evolution method.
	 * Uses EvoControl to determine whether hill climber
	 * or genetic algorithm solution should be used. 
	 */
	public void evolve(){
		if (this.control.alg == EvoControl.Evolution.HC){
			this.evolveHC();
		} else if (this.control.alg == EvoControl.Evolution.GA)  {
			this.evolveGA();
		}
	}
	/** Hill Climbing image evolution solution.
	 * Performs evolution of an initial random image until it is
	 * generated image is past threshold for being similar to target.
	 */
	private void evolveHC(){
		// get start time
		//long startTime = System.currentTimeMillis();
		// create initial random dna
		this.best = new EvoImg(this.control);
		this.best.render();
		this.best.compare(this.sourceImg);
		// evolution loop
		int cntGen=0; // generation counter
		EvoImg test;
		while (this.best==null 
				|| this.best.fitness < this.control.threshold
				|| (this.control.maxGenerations>0 && this.control.maxGenerations>=cntGen)){
			// copy and mutate current best
			test = this.best.copyDna();
			test.mutate(this.control);
			test.render();
			test.compare(sourceImg);
			// compare fitness of next against best
			if(test.fitness > this.best.fitness){
				this.best = test;
				//evolveOutput(startTime, cntGen);
			}
			cntGen++;
		}
	}
	/** Genetic Algorithm image evolution solution.
	 * Performs evolution of an initial random image until it is
	 * generated image is past threshold for being similar to target.
	 */
	private void evolveGA(){
		// get start time
		//long startTime = System.currentTimeMillis();
		// create initial random population
		this.population = new EvoImg[this.control.population];
		for (int i=0; i<this.population.length; i++){
			this.population[i] = new EvoImg(this.control);
		}
		// evolution loop
		int cntGen=0; // generation counter
		while (this.best==null 
				|| this.best.fitness < this.control.threshold
				|| (this.control.maxGenerations>0 && this.control.maxGenerations>=cntGen)){
			// render each image and calculate fitness
			for (EvoImg a : this.population){
				if(a.image==null){
					a.render();
					a.compare(this.sourceImg);
				}
			}
			// sort the population by fitness
			Arrays.sort(this.population, EvoImg.FitnessComparator);
			
			// check if new best found
			if( this.best==null ||
					this.population[this.control.population-1].fitness > this.best.fitness){
				this.best = this.population[this.control.population-1];
				//evolveOutput(startTime, cntGen);
			}
			// create next population
			this.nextGeneration();
			// increment generation counter
			cntGen++;
		}
	}
	/** Helper function for evolveGA.
	 * Computes the next generation of the population using
	 * parameters and flags set in the EvoControl.
	 */
	private void nextGeneration(){
		// initializing
		EvoImg[] newPop = new EvoImg[this.population.length];
		int newPopPtr=0;
		// find section of fit parents
		int parentRange;
		if(this.control.rndCutoff){
			parentRange = this.population.length;
		} else {
			parentRange = (int)(this.population.length*this.control.parentCutoff);
		}
		
		// if not killing parents put into new population
		if (!this.control.killParents){
			int parentCutIndex = this.population.length-(int)(this.population.length*this.control.parentCutoff);
			for (int i=parentCutIndex; i<this.population.length; i++){
				newPop[newPopPtr++] = this.population[i];
			}
		}
		
		// Breed randomly selected parents to fill out rest of population
		while(newPopPtr<newPop.length){
			// randomly select parents from in range
			// using gaussian distribution to give higher fitness
			// parents priority
			EvoImg[] tmp = EvoImg.crossBreed(
					this.population[this.population.length-(int)Math.abs(rndSrc.get().nextGaussian()*parentRange)-1],
					this.population[this.population.length-(int)Math.abs(rndSrc.get().nextGaussian()*parentRange)-1],
					this.control.uniformCross);
			// mutate children
			tmp[0].mutate(this.control);
			tmp[1].mutate(this.control);
			// add to population
			if(newPopPtr<newPop.length){
				newPop[newPopPtr++] = tmp[0];
			}
			if(newPopPtr<newPop.length){
				newPop[newPopPtr++] = tmp[1];
			}
		}
		// set population to be the new population
		this.population = newPop;
	}
	
	/* Input-Output methods */
	
	/** Helper function for outputting when new best found
	 * @param startTime start time of evolution in milliseconds (i.e. System.currentTimeMillis())
	 * @param gen current generation count
	 */
	@SuppressWarnings("unused")
	private void evolveOutput(long startTime, long gen){
		// attempt to output current best image
		if(this.outImg!=null && this.best!=null && this.best.image!=null){
			ImgEvolution.outputImage(this.best.image, "png", this.outImg);
		}
		// attempt to append log
		if (this.log!=null){
			try {
				this.log.write(this.name+" - new best found!"
						+"\tn="+gen
						+"\tfitness="+this.best.fitness
						+"\telapsedTime="+fmtTimeDiff(System.currentTimeMillis(),startTime)
						+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/** Method for showing time difference in human-readable format.
	 * Best used with System.currentTimeMillis().
	 * @param mills1 long start time
	 * @param mills2 long end time
	 * @return String time difference in hh:mm:ss.sss format.
	 */
	public static String fmtTimeDiff(long mills1, long mills2){
		long diff = (mills2>mills1)?mills2-mills1:mills1-mills2;
		long hrs = diff/3600000;
		long mins = diff/60000-hrs*60;
		double secs = diff/1000.0-(double)mins*60-(double)hrs*3600;
		return String.format("%02d",hrs)+":"
				+String.format("%02d",mins)+":"
				+String.format("%02.3f",secs);
	}
	/** Read an image from a file
	 * @param image File to be 
	 * @return
	 */
	public static BufferedImage getSourceImage(File image){
		BufferedImage tmp = null;
		try {
			tmp = ImageIO.read(image);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return tmp;
	}
	/** Output an image to file
	 * @param img BufferedImage to write to file
	 * @param fmt String file format (e.g. "png" or "jpeg")
	 * @param file File to output image to
	 * @see ImageIO.getWriterFormatNames()
	 */
	public static void outputImage(BufferedImage img, String fmt, File file){
		try {
			ImageIO.write(img, fmt, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/* Runnable implementation */
	/** implementation of Runnable.
	 * runs the evolve() method with current control parameters
	 */
	public void run() {
		// start log output
		if (this.log!=null){
			try {
				this.log.write("Evolution Starting - "+this.name+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.evolve();
		// finalize log output
		if (this.log!=null){
			try {
				this.log.write("Evolution Complete - "+this.name+"\n");
				this.log.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
