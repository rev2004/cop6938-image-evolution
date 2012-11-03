package imageEvolve;

import imageEvolve.EvoImg;
import imageEvolve.EvoControl.Evolution;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
public class ImgEvolution {
		
	private static final ThreadLocal<Random> rndSrc =
			new ThreadLocal <Random> () {
				@Override protected Random initialValue() { return new Random(); }
	};
	
	/* Evolution instance variables */
	EvoControl control;
	private BufferedImage sourceImg;
	private EvoImg best;
	private EvoImg[] population;
	
	
	/** Default constructor
	 */
	public ImgEvolution(){
		this.control = new EvoControl();
		this.best = null;
		this.population = null;
	}
	
	/** Driver for starting up a single instance of image evolution.
	 * @param args arguments are ignored
	 */
	public static void main(String[] args){
		// Make an instance 
		ImgEvolution e = new ImgEvolution();
		// Get target image
		e.sourceImg = ImgEvolution.getSourceImage(new File("canvas.png"));
		// Setup evolution parameters
		e.control.size_x = e.sourceImg.getWidth();
		e.control.size_y = e.sourceImg.getHeight();
		e.control.polygons = 100;
		e.control.vertices = 6;
		e.control.population = 40;
		e.control.alg = Evolution.GA;
		e.control.initColor = EvoControl.InitColor.RAND;
		e.control.mutationMode = EvoControl.Mutation.SOFT;
		e.control.comparison = EvoControl.CompMode.DIFF;
		e.control.mutationChance = 0.2;
		e.control.mutationDegree = 0.1;
		e.control.uniformCross = true;
		e.control.parentCutoff = 0.1;
		e.control.killParents = false;
		e.control.rndCutoff = false;
		e.control.threshold = 0.985;
		// Run evolution
		System.out.println("Evolution Starting");
		e.evolve();
		System.out.println("Evolution Complete");
	}
	
	
	/** Generic evolution method.
	 * Uses EvoControl to determine whether hill climber
	 * or genetic algorithm solution should be used. 
	 */
	public void evolve(){
		if (this.control.alg == EvoControl.Evolution.HC){
			this.evolveGA();
		} else if (this.control.alg == EvoControl.Evolution.GA)  {
			this.evolveGA();
		}
	}
	
	/** Hill Climbing image evolution solution.
	 * Performs evolution of an initial random image until it is
	 * generated image is past threshold for being similar to target.
	 */
	private void evolveHC(){
		// Create initial random dna
		this.best = new EvoImg(this.control);
		this.best.render();
		this.best.compare(this.sourceImg);
		ImgEvolution.outputImage(this.best.image, "png", new File("best0.png"));
		// Evolution loop
		int cntGen=0; // generation counter
		EvoImg test;
		while (this.best.fitness < this.control.threshold){
			// copy and mutate current best
			test = this.best.copyDna();
			test.mutate(this.control);
			test.render();
			test.compare(sourceImg);
			// compare fitness of next against best
			if(test.fitness > this.best.fitness){
				this.best = test;
				ImgEvolution.outputImage(this.best.image, "png", new File("best.png"));
				System.out.println("New best found! n="+cntGen+" fitness="+this.best.fitness);
			}
			cntGen++;
		}
	}
	
	/** Genetic Algorithm image evolution solution.
	 * Performs evolution of an initial random image until it is
	 * generated image is past threshold for being similar to target.
	 */
	private void evolveGA(){
		// Create initial random population
		this.population = new EvoImg[this.control.population];
		for (int i=0; i<this.population.length; i++){
			this.population[i] = new EvoImg(this.control);
		}
		// Evolution loop
		int cntGen=0; // generation counter
		while (this.best==null || this.best.fitness < this.control.threshold){
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
				//ImgEvolution.outputImage(this.best.image, "png", new File("best"+cntGen+".png"));
				ImgEvolution.outputImage(this.best.image, "png", new File("best.png"));
				System.out.println("New best found! n="+cntGen+" fitness="+this.best.fitness);
			}
			// create next population
			this.nextGeneration();
			// increment generation counter
			cntGen++;
			/* break loop for debugging
			if (cntGen>1000){
				break;
			} //*/
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
					this.population[this.population.length-(int)Math.abs(rndSrc.get().nextGaussian()*parentRange)],
					this.population[this.population.length-(int)Math.abs(rndSrc.get().nextGaussian()*parentRange)],
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

}
