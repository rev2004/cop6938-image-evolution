package imageEvolveCore;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.Comparator;
import java.util.Random;

import imageEvolveCore.ColorPolygon;
import imageEvolveCore.EvoControl;

/** Class representing a evolved image
 *  
 */
public class EvoImg {
	
	/** Source of randomness.
	 * ThreadLocal used to avoid blocking when used by
	 * concurrent threads
	 */
	private static final ThreadLocal<Random> rndSrc =
			new ThreadLocal <Random> () {
				@Override protected Random initialValue() { return new Random(); }
	};
	
	/* Static variables */
	
	/* Instance variables */
	int x_max, y_max, poly, vert;
	ColorPolygon[] dna;
	BufferedImage image;
	double fitness;
	int generation;
	
	/* Constructors */
	
	/** Default constructor - sets basic testing values into parameters.
	 * maximum image width = 100px; maximum image height = 100px; no DNA or image;
	 */
	public EvoImg(){
		x_max = 100;
		y_max = 100;
		dna = null;
		image = null;
	}
	/** Constructor - initialize basic parameters, but not image or DNA.
	 * Does initialize space to hold polygons.
	 * @param x int maximum x-coordinate (width) of image
	 * @param y int maximum y-coordinate (height) of image
	 * @param p int number of polygons
	 * @param v int number of vertices per polygon
	 */
	public EvoImg(int x, int y, int p, int v){
		x_max = x;
		y_max = y;
		poly = p;
		vert = v;
		dna = new ColorPolygon[p];
		image = null;
	}
	/** Constructor - fully initialize EvoImg.
	 * Uses parameters in EvoControl to specify maximum image dimensions,
	 * polygon count, vertices per polygon and how to generate random polygons
	 * for initial DNA data. Does not render the image.
	 * @param c
	 */
	public EvoImg(EvoControl c){
		x_max = c.size_x;
		y_max = c.size_y;
		poly = c.polygons;
		vert = c.vertices;
		dna = initDna(c);
		image = null;
	}
	/** Generate randomized set of polygons (DNA). Takes
	 * an EvoControl object to pass parameters.
	 * @param c EvoControl providing parameters for DNA
	 * @return ColorPolygon[] with randomized DNA
	 * @
	 */
	private static ColorPolygon[] initDna(EvoControl c){
		ColorPolygon[] tmp = new ColorPolygon[c.polygons];
		for (int i=0; i<c.polygons; i++){
			tmp[i] = new ColorPolygon();
			if(c.initColor==EvoControl.InitColor.BLACK){
				tmp[i].color = Color.BLACK;
			} else if (c.initColor==EvoControl.InitColor.WHITE){
				tmp[i].color = Color.WHITE;
			} else {
				tmp[i].color = new Color(
						rndSrc.get().nextInt(255),
						rndSrc.get().nextInt(255),
						rndSrc.get().nextInt(255),
						rndSrc.get().nextInt(255));
			}
			for (int j=0; j<c.vertices; j++){
				tmp[i].addPoint(rndSrc.get().nextInt(c.size_x), rndSrc.get().nextInt(c.size_y));
			}
		}
		return tmp;
	}
	
	
	/* Inspectors */
	/** Get current DNA
	 * @return deep copy of DNA
	 */
	public ColorPolygon[] getDna(){
		ColorPolygon[] tmp = new ColorPolygon[this.dna.length];
		for (int i=0; i<this.dna.length; i++){
			try {
				// check that polygon exists
				if (this.dna[i]!=null && this.dna[i].color!=null && this.dna[i].npoints>0){
					// if exists clone and add to tmp array
					tmp[i] = (ColorPolygon)this.dna[i].clone();
				}
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
		return tmp;
	}
	/** Get current image
	 * @return deep copy of image
	 */
	public Image getImage(){
		ColorModel cm = this.image.getColorModel();
		WritableRaster raster = this.image.copyData(null);
		return new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), null);
	}
	/** get current fitness
	 * @return current fitness
	 */
	public double getFitness(){
		return this.fitness;
	}
	/** get current generation
	 * @return current generation
	 */
	public int getGeneration(){
		return this.generation;
	}
	/** Comparator based on fitness.
	 * Orders low fitness images before high fitness images
	 */
	public static Comparator<EvoImg> FitnessComparator = new Comparator<EvoImg>() {
		public int compare(EvoImg a, EvoImg b){
			return (a.fitness<b.fitness)?-1:(a.fitness==b.fitness)?0:1;
		}
	};
	
	/* Compilation */
	
	/** Regenerate the image from current DNA
	 */
	public void render(){
		// clear image by creating a new one
		this.image = new BufferedImage(x_max,y_max,BufferedImage.TYPE_4BYTE_ABGR);
		Graphics g = this.image.getGraphics();
		for (int i=0; i<this.dna.length; i++){
			// check that polygon exists
			if (this.dna[i]!=null && this.dna[i].color!=null && this.dna[i].npoints>0){
				g.setColor(this.dna[i].color);
				g.fillPolygon(this.dna[i]);
			}
		}
	}
	
	/** Compare current image to another image, calculates percent
	 *  difference  pixel by pixel and sets as fitness
	 * @param image BufferedImage to compare to
	 */
	public void compare(BufferedImage image){
		if (this.image!=null && image!=null){
			// reset fitness
			// check image bounds
			if (this.x_max==image.getWidth() && this.y_max==image.getHeight()){
				// initialize difference
				long d = 0;
				// iterate images and check each pixel
				for (int i=0; i<this.x_max; i++){
					for (int j=0; j<this.y_max; j++){
						// get pixel from each image
						int px_this = this.image.getRGB(i,j);
						int px_img = image.getRGB(i,j);
						// add difference between RGB components
						d += Math.abs(((px_this)&255)-((px_img)&255));
						d += Math.abs(((px_this>>8)&255)-((px_img>>8)&255));
						d += Math.abs(((px_this>>16)&255)-((px_img>>16)&255));
						d += Math.abs(((px_this>>24)&255)-((px_img>>24)&255));
					}
				}
				// set fitness as ratio
				this.fitness =  1.0 - ((double)(d))/((double)(this.x_max*this.y_max*765));
			} else {
				this.fitness=0.0;
			}
		} else {
			this.fitness = 0.0;
		}
	}
	/** Compare current image to another image, calculates percent
	 *  difference  pixel by pixel and sets as fitness
	 * @param image BufferedImage to compare to
	 * @param c EvoControl to decide fitness metric algorithm
	 */
	public void compare(EvoControl c, BufferedImage image){
		if (this.image!=null && image!=null){
			// reset fitness
			// check image bounds
			if (this.x_max==image.getWidth() && this.y_max==image.getHeight()){
				// initialize difference
				long d = 0;
				// iterate images and check each pixel
				for (int i=0; i<this.x_max; i++){
					for (int j=0; j<this.y_max; j++){
						// get pixel from each image
						int px_this = this.image.getRGB(i,j);
						int px_img = image.getRGB(i,j);
						// add difference between RGB components
						if (c.comparison==EvoControl.CompMode.DIFF){
							d += Math.abs(((px_this)&255)-((px_img)&255));
							d += Math.abs(((px_this>>8)&255)-((px_img>>8)&255));
							d += Math.abs(((px_this>>16)&255)-((px_img>>16)&255));
							d += Math.abs(((px_this>>24)&255)-((px_img>>24)&255));
						} else if (c.comparison==EvoControl.CompMode.DSQRD){
							int t;
							t = Math.abs(((px_this)&255)-((px_img)&255));
							d += t*t;
							t = Math.abs(((px_this>>8)&255)-((px_img>>8)&255));
							d += t*t;
							t = Math.abs(((px_this>>16)&255)-((px_img>>16)&255));
							d += t*t;
							t = Math.abs(((px_this>>24)&255)-((px_img>>24)&255));
							d += t*t;
						} else if (c.comparison==EvoControl.CompMode.HARD){
							d += (px_this!=px_img)?1:0;
						}
					}
				}
				// set fitness as ratio
				if (c.comparison==EvoControl.CompMode.DIFF){
					this.fitness =  1.0 - ((double)(d))/((double)(this.x_max*this.y_max*765));
				} else if (c.comparison==EvoControl.CompMode.DSQRD){
					this.fitness =  1.0 - ((double)(d))/((double)(this.x_max*this.y_max*195075));
				} else if (c.comparison==EvoControl.CompMode.HARD){
					this.fitness =  1.0 - ((double)(d))/((double)(this.x_max*this.y_max));
				} else {
					this.fitness = 0.0;
				}
			} else {
				this.fitness=0.0;
			}
		} else {
			this.fitness = 0.0;
		}
	}
	
	/* DNA manipulation methods */
	
	/** Create a copy of this image with DNA, but without image render or fitness
	 * @return EvoImg partial copy of current image
	 */
	public EvoImg copyDna(){
		EvoImg tmp = new EvoImg(this.x_max, this.y_max, this.poly, this.vert);
		for (int i=0; i<this.dna.length; i++){
			// check that polygon exists
			if (this.dna[i]!=null && this.dna[i].color!=null && this.dna[i].npoints>0){
				try {
					tmp.dna[i] = (ColorPolygon)this.dna[i].clone();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}
		}
		return tmp;
	}
	
	/** Mutation method - uses EvoControl to decide correct algorithm to use.
	 * @param c EvoControl to determine algorithm of mutation and mutation parameters
	 */
	public void mutate(EvoControl c){
		if (c.mutationMode==EvoControl.Mutation.GAUSS){
			mutateGauss(c);
		} else if (c.mutationMode==EvoControl.Mutation.SOFT) {
			mutateSoft(c);
		} else if (c.mutationMode==EvoControl.Mutation.MEDIUM) {
			mutateMedium(c);
		} else if (c.mutationMode==EvoControl.Mutation.HARD) {
			mutateHard(c);
		} else if (c.mutationMode==EvoControl.Mutation.PROB) {
			mutateProb(c);
		}
	}
	/** Mutation method - picks one polygon and only one attribute of that polygon.
	 * uses a gaussian distribution when changing that parameter from current value.
	 * Large changes are unlikely.
	 */
	private void mutateGauss(EvoControl c){
		int select;
		do {
			select = EvoImg.rndSrc.get().nextInt(this.dna.length);
		} while (this.dna[select]==null || this.dna[select].color==null || this.dna[select].npoints<=0);
		double action = 2.0*EvoImg.rndSrc.get().nextDouble();
		
		// if between 0 and 1 mutate color
		if (action < 1.0){
			if (action < 0.25) { // mutate red
				this.dna[select].color = new Color(
						(int)gaussRange(this.dna[select].color.getRed(),255.0/2,0.0,255.0),
						this.dna[select].color.getGreen(),
						this.dna[select].color.getBlue(),
						this.dna[select].color.getAlpha());
				//System.out.println("selected "+select+" mutate color - red");
			}
			else if (action < 0.5) { // mutate green
				this.dna[select].color = new Color(
						this.dna[select].color.getRed(),
						(int)gaussRange(this.dna[select].color.getGreen(),255.0/2,0.0,255.0),
						this.dna[select].color.getBlue(),
						this.dna[select].color.getAlpha());
				//System.out.println("selected "+select+" mutate color - green");
			}
			else if (action < 0.75) { // mutate blue
				this.dna[select].color = new Color(
						this.dna[select].color.getRed(),
						this.dna[select].color.getGreen(),
						(int)gaussRange(this.dna[select].color.getBlue(),255.0/2,0.0,255.0),
						this.dna[select].color.getAlpha());
				//System.out.println("selected "+select+" mutate color - blue");
			}
			else { // mutate alpha
				this.dna[select].color = new Color(
						this.dna[select].color.getRed(),
						this.dna[select].color.getGreen(),
						this.dna[select].color.getBlue(),
						(int)gaussRange(this.dna[select].color.getAlpha(),255.0/2,0.0,255.0));
				//System.out.println("selected "+select+" mutate color - alpha");
			}
		}
		// if between 1 and 2 mutate vertex
		else {
			// pick a vertex
			int vert = rndSrc.get().nextInt(this.dna[select].npoints);
			// edit x or y coordinate of vertex
			if (action < 1.5){
				this.dna[select].xpoints[vert] = (int)gaussRange(
						this.dna[select].xpoints[vert],(double)x_max/2,0.0,x_max);
				//System.out.println("selected "+select+" mutate vertex "+vert+" - x");
			} else {
				this.dna[select].ypoints[vert] = (int)gaussRange(
						this.dna[select].ypoints[vert],(double)y_max/2,0.0,y_max);
				//System.out.println("selected "+select+" mutate vertex "+vert+" - y");
			}
		}
	}
	/** Helper function for giving a gaussian (bell) distribution of values on a range
	 *  with hard minimum and maximum values. If the distribution returns a value
	 *  outside the min-max range it will be replaced with the nearest bound.
	 * @param center double center of the probability density distribution.
	 * @param range double range of the distribution (center-1/2 to center+1/2)
	 * @param min double minimum value to return
	 * @param max double maximum value to return
	 * @return
	 */
	public static double gaussRange(double center, double range, double min, double max){
		double rtn = EvoImg.rndSrc.get().nextGaussian()*range+center-range/2;
		rtn = (rtn < min) ? min : (rtn > max)? max : rtn;
		return rtn;
	}
	/** Mutation method - picks one polygon and only one attribute of that polygon.
	 * uses a uniform distribution, but limited to very small changes on parameter.
	 * Large changes are impossible to that one parameter.
	 */
	private void mutateSoft(EvoControl c){
		int select;
		// Pick a valid polygon
		do {
			select = EvoImg.rndSrc.get().nextInt(this.dna.length);
		} while (this.dna[select]==null || this.dna[select].color==null || this.dna[select].npoints<=0);
		
		double action = 2.0*EvoImg.rndSrc.get().nextDouble();
		
		// if between 0 and 1 mutate color
		if (action < 1.0){
			// do the mutation
			if (action < 0.25) { // mutate red
				this.dna[select].color = new Color(
						deltaRange(this.dna[select].color.getRed(),19,0,255),
						this.dna[select].color.getGreen(),
						this.dna[select].color.getBlue(),
						this.dna[select].color.getAlpha());
				//System.out.println("selected "+select+" mutate color - red");
			}
			else if (action < 0.5) { // mutate green
				this.dna[select].color = new Color(
						this.dna[select].color.getRed(),
						deltaRange(this.dna[select].color.getGreen(),19,0,255),
						this.dna[select].color.getBlue(),
						this.dna[select].color.getAlpha());
				//System.out.println("selected "+select+" mutate color - green");
			}
			else if (action < 0.75) { // mutate blue
				this.dna[select].color = new Color(
						this.dna[select].color.getRed(),
						this.dna[select].color.getGreen(),
						deltaRange(this.dna[select].color.getBlue(),19,0,255),
						this.dna[select].color.getAlpha());
				//System.out.println("selected "+select+" mutate color - blue");
			}
			else { // mutate alpha
				this.dna[select].color = new Color(
						this.dna[select].color.getRed(),
						this.dna[select].color.getGreen(),
						this.dna[select].color.getBlue(),
						deltaRange(this.dna[select].color.getAlpha(),19,0,255));
				//System.out.println("selected "+select+" mutate color - alpha");
			}
		}
		// if between 1 and 2 mutate vertex
		else {
			// decide the amount of change (to position)
			// pick a vertex
			int vert = rndSrc.get().nextInt(this.dna[select].npoints);
			// edit x or y coordinate of vertex
			if (action < 1.5){
				this.dna[select].xpoints[vert] = deltaRange(this.dna[select].xpoints[vert],
						this.x_max/10,0,this.x_max);
				//System.out.println("selected "+select+" mutate vertex "+vert+" - x");
			} else {
				this.dna[select].ypoints[vert] = deltaRange(this.dna[select].ypoints[vert],
						this.y_max/10,0,this.y_max);
				//System.out.println("selected "+select+" mutate vertex "+vert+" - y");
			}
		}
	}
	/** Helper function for giving a uniform distribution of values on a range with
	 *  hard minimum and maximum values. If the distribution returns a value
	 *  outside the min-max range it will be replaced with the nearest bound.
	 * @param center int center of value range.
	 * @param range int range of the distribution (center-1/2 to center+1/2)
	 * @param min int minimum value to return
	 * @param max int maximum value to return
	 * @return
	 */
	public static int deltaRange(int center, int range, int min, int max){
		int rtn = EvoImg.rndSrc.get().nextInt(range)+center-range/2;
		rtn = (rtn < min) ? min : (rtn > max)? max : rtn;
		return rtn;
	}
	/** Mutation method - picks one polygon and only one attribute of that polygon.
	 * uses a uniform distribution when changing that parameter from current value.
	 * Large changes are likely to that one parameter.
	 */
	private void mutateMedium(EvoControl c){
		int select;
		// Pick a valid polygon
		do {
			select = EvoImg.rndSrc.get().nextInt(this.dna.length);
		} while (this.dna[select]==null || this.dna[select].color==null || this.dna[select].npoints<=0);
		
		double action = 2.0*EvoImg.rndSrc.get().nextDouble();
		
		// if between 0 and 1 mutate color
		if (action < 1.0){
			if (action < 0.25) { // mutate red
				this.dna[select].color = new Color(
						rndSrc.get().nextInt(255),
						this.dna[select].color.getGreen(),
						this.dna[select].color.getBlue(),
						this.dna[select].color.getAlpha());
				//System.out.println("selected "+select+" mutate color - red");
			}
			else if (action < 0.5) { // mutate green
				this.dna[select].color = new Color(
						this.dna[select].color.getRed(),
						rndSrc.get().nextInt(255),
						this.dna[select].color.getBlue(),
						this.dna[select].color.getAlpha());
				//System.out.println("selected "+select+" mutate color - green");
			}
			else if (action < 0.75) { // mutate blue
				this.dna[select].color = new Color(
						this.dna[select].color.getRed(),
						this.dna[select].color.getGreen(),
						rndSrc.get().nextInt(255),
						this.dna[select].color.getAlpha());
				//System.out.println("selected "+select+" mutate color - blue");
			}
			else { // mutate alpha
				this.dna[select].color = new Color(
						this.dna[select].color.getRed(),
						this.dna[select].color.getGreen(),
						this.dna[select].color.getBlue(),
						rndSrc.get().nextInt(255));
				//System.out.println("selected "+select+" mutate color - alpha");
			}
		}
		// if between 1 and 2 mutate vertex
		else {
			// pick a vertex
			int vert = rndSrc.get().nextInt(this.dna[select].npoints);
			// edit x or y coordinate of vertex
			if (action < 1.5){
				this.dna[select].xpoints[vert] = rndSrc.get().nextInt(x_max);
				//System.out.println("selected "+select+" mutate vertex "+vert+" - x");
			} else {
				this.dna[select].ypoints[vert] = rndSrc.get().nextInt(y_max);
				//System.out.println("selected "+select+" mutate vertex "+vert+" - y");
			}
		}
	}
	/** Mutation method - picks one polygon and changes all parameters of that polygon.
	 * uses a uniform distribution when changing parameters from current value.
	 * Large changes are likely and image changes rapidly.
	 */
	private void mutateHard(EvoControl c){
		int select;
		// Pick a valid polygon
		do {
			select = EvoImg.rndSrc.get().nextInt(this.dna.length);
		} while (this.dna[select]==null || this.dna[select].color==null || this.dna[select].npoints<=0);
		// change all parameters
		this.dna[select].color = new Color(
				rndSrc.get().nextInt(255),
				rndSrc.get().nextInt(255),
				rndSrc.get().nextInt(255),
				rndSrc.get().nextInt(255));
		int vert = rndSrc.get().nextInt(this.dna[select].npoints);
		this.dna[select].xpoints[vert] = rndSrc.get().nextInt(x_max);
		this.dna[select].ypoints[vert] = rndSrc.get().nextInt(y_max);
	}
	/** Mutation method - picks one polygon and only one attribute of that polygon.
	 * uses a uniform distribution, but limited to specified percent. Probability of 
	 * mutation occurring is also specified. Otherwise similar to SOFT mutation.
	 */
	private void mutateProb(EvoControl c){
		if (rndSrc.get().nextDouble()<=c.mutationChance){
			int select;
			// Pick a valid polygon
			do {
				select = EvoImg.rndSrc.get().nextInt(this.dna.length);
			} while (this.dna[select]==null || this.dna[select].color==null || this.dna[select].npoints<=0);
			
			double action = 2.0*EvoImg.rndSrc.get().nextDouble();
			
			// if between 0 and 1 mutate color
			if (action < 1.0){
				// do the mutation
				if (action < 0.25) { // mutate red
					this.dna[select].color = new Color(
							deltaRange(this.dna[select].color.getRed(),(int)(255*c.mutationDegree),0,255),
							this.dna[select].color.getGreen(),
							this.dna[select].color.getBlue(),
							this.dna[select].color.getAlpha());
					//System.out.println("selected "+select+" mutate color - red");
				}
				else if (action < 0.5) { // mutate green
					this.dna[select].color = new Color(
							this.dna[select].color.getRed(),
							deltaRange(this.dna[select].color.getGreen(),(int)(255*c.mutationDegree),0,255),
							this.dna[select].color.getBlue(),
							this.dna[select].color.getAlpha());
					//System.out.println("selected "+select+" mutate color - green");
				}
				else if (action < 0.75) { // mutate blue
					this.dna[select].color = new Color(
							this.dna[select].color.getRed(),
							this.dna[select].color.getGreen(),
							deltaRange(this.dna[select].color.getBlue(),(int)(255*c.mutationDegree),0,255),
							this.dna[select].color.getAlpha());
					//System.out.println("selected "+select+" mutate color - blue");
				}
				else { // mutate alpha
					this.dna[select].color = new Color(
							this.dna[select].color.getRed(),
							this.dna[select].color.getGreen(),
							this.dna[select].color.getBlue(),
							deltaRange(this.dna[select].color.getAlpha(),(int)(255*c.mutationDegree),0,255));
					//System.out.println("selected "+select+" mutate color - alpha");
				}
			}
			// if between 1 and 2 mutate vertex
			else {
				// decide the amount of change (to position)
				// pick a vertex
				int vert = rndSrc.get().nextInt(this.dna[select].npoints);
				// edit x or y coordinate of vertex
				if (action < 1.5){
					this.dna[select].xpoints[vert] = deltaRange(this.dna[select].xpoints[vert],
							(int)(this.x_max*c.mutationDegree),0,this.x_max);
					//System.out.println("selected "+select+" mutate vertex "+vert+" - x");
				} else {
					this.dna[select].ypoints[vert] = deltaRange(this.dna[select].ypoints[vert],
							(int)(this.y_max*c.mutationDegree),0,this.y_max);
					//System.out.println("selected "+select+" mutate vertex "+vert+" - y");
				}
			}
		}
	}
	
	/** Crosses the DNA to two parents EvoImg to create two child EvoImg objects
	 * which are returned in an array. Assumes that the parameters for both parents
	 * are the same (image width, image height, maximum polygons, maximum vertices).
	 * Polygons of parents are cloned in the children.
	 * @param A EvoImg first parent
	 * @param B EvoImg second parent
	 * @param uniform boolean if crossing should be uniform else single splice
	 * @return EvoImg array contain two child EvoImg objects
	 */
	public static EvoImg[] crossBreed(EvoImg A, EvoImg B, boolean uniform){
		// initialize children
		EvoImg[] tmp = new EvoImg[2];
		tmp[0] = new EvoImg(A.x_max, A.y_max, A.poly, A.vert);
		tmp[1] = new EvoImg(A.x_max, A.y_max, A.poly, A.vert);
		
		int pmax = (A.poly<=B.poly) ? A.poly : B.poly;
		try {
			// Uniform splice (random split each polygon between each child)
			if(uniform){
				for (int i=0; i<pmax; i++){
					if (rndSrc.get().nextBoolean()){
						tmp[0].dna[i] = (ColorPolygon)A.dna[i].clone();
						tmp[1].dna[i] = (ColorPolygon)B.dna[i].clone();
					} else {
						tmp[0].dna[i] = (ColorPolygon)B.dna[i].clone();
						tmp[1].dna[i] = (ColorPolygon)A.dna[i].clone();
					}
				}
			}
			// Else make a single splice
			else {
				int splice = rndSrc.get().nextInt(pmax-1);
				for (int i=0; i<pmax; i++){
					if (i<=splice){
						tmp[0].dna[i] = (ColorPolygon)A.dna[i].clone();
						tmp[1].dna[i] = (ColorPolygon)B.dna[i].clone();
					} else {
						tmp[0].dna[i] = (ColorPolygon)B.dna[i].clone();
						tmp[1].dna[i] = (ColorPolygon)A.dna[i].clone();
					}
				}
			}
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return tmp;
	}
	
}
