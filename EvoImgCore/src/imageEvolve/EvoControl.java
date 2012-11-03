package imageEvolve;

/** Simple class for recording and communicating evolution control parameters.
 * Contains parameters which are required to initialize images and DNA, 
 * specification of which algorithms to use for fitness, mutation and breeding,
 * parameters to control mutation effects and the threshold of required fitness
 * for the algorithm to complete.
 */
public class EvoControl {
	
	/* Evolution parameter enumerations */
	/** Mutation algorithm enumeration.
	 * <br>GAUSS - uses Gaussian distribution to limit degree of mutation on single parameter at once.
	 * <br>SOFT - uses a percentage range to limit degree of mutation on single parameter at once.
	 * <br>MEDIUM - no limit on degree of mutation for single parameter at once.
	 * <br>HARD - no limit on degree of mutation on all parameters at once.
	 * <br>PROB - same as soft but with probability of mutation occurring.
	 */
	public enum Mutation { GAUSS, SOFT, MEDIUM, HARD, PROB }
	/** Mutation algorithm enumeration.
	 * <br>RAND - assign each polygon a random color on initialization.
	 * <br>WHITE - make each polygon white on initialization.
	 * <br>BLACK - make each polygon black on initialization.
	 */
	public enum InitColor { RAND, WHITE, BLACK }
	/** Mutation algorithm enumeration.
	 * <br>DIFF - difference on each channel (ARGB) for each pixel.
	 * <br>DSQRD - difference squared for each channel (ARGB) for each pixel.
	 * <br>HARD - match or no match for each pixel.
	 */
	public enum CompMode { DIFF, DSQRD, HARD} 
	
	/* Instance properties */
	public int size_x; // Width of image
	public int size_y; // Height of image
	public int polygons; // Number of polygons
	public int vertices; // Number of points in each polygon
	public InitColor initColor; // Polygon initial color
	public Mutation mutationMode; // Mutation algorithm
	public CompMode comparison; // Method of comparison for fitness
	public double mutationChance; // Chance mutation will occur
	public double mutationDegree; // Amount of mutation
	public boolean killParents; // Flag to remove parents from next generation
	public boolean uniformCross; // Flag if uniform crossover to be made (otherwise random one-splice)
	public double parentCutoff; // Percentage of parents selected
	public boolean rndCutoff; // Flag if parent selection should be weighted random (otherwise top X%)
	public double threshold; // Percentage fitness at which evolution can end
	
	/** Default constructor.
	 * Requires many parameters to be set after construction for correct function.
	 */
	public EvoControl(){
		size_x = 0;
		size_y = 0;
		polygons = 0;
		vertices = 0;
		initColor = InitColor.RAND;
		mutationMode = Mutation.MEDIUM;
		comparison = CompMode.DIFF;
		mutationChance = 0.0;
		mutationDegree = 0.0;
		killParents = false;
		uniformCross = false;
		parentCutoff = 0.0;
		rndCutoff = false;
		threshold = 0.0;
	}

}
