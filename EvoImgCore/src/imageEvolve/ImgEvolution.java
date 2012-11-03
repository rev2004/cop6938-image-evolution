package imageEvolve;

import imageEvolve.EvoImg;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
		
	/* Evolution instance parameters */

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
		e.control.mutationMode = EvoControl.Mutation.MEDIUM;
		e.control.initColor = EvoControl.InitColor.RAND;
		e.control.comparison = EvoControl.CompMode.DIFF;
		e.control.threshold = 0.985;
		// Run evolution
		System.out.println("Evolution Starting");
		e.evolveHC();
		System.out.println("Evolution Complete");
	}
	
	/** Hill Climbing image evolution solution.
	 * Performs evolution of an initial random image until it is
	 * generated image is past threshold for being similar to target.
	 */
	public void evolveHC(){
		// Create initial random dna
		this.best = new EvoImg(this.control);
		this.best.render();
		this.best.compare(this.sourceImg);
		ImgEvolution.outputImage(this.best.image, "png", new File("best0.png"));
		// Evolution loop
		int i=1;
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
				System.out.println("New best found! n="+i+" fitness="+this.best.fitness);
			}
			i++;
		}
	}
	
	/** Genetic Algorithm image evolution solution.
	 * Performs evolution of an initial random image until it is
	 * generated image is past threshold for being similar to target.
	 */
	public void evolveGA(){
		
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
