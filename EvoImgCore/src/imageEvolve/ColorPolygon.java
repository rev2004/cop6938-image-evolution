package imageEvolve;

import java.awt.Color;
import java.awt.Polygon;

/** Class extends polygon (java.awt.Polygon) to add a color property (java.awt.Color)
 * @see java.awt.Color
 * @see java.awt.Polygon
 */
public class ColorPolygon extends Polygon implements Cloneable{
	// Color Polygon not serializable (yet), just inherited from Polygon
	private static final long serialVersionUID = -5418103289546199594L;
	
	// Color property (default scope)
	Color color;
	
	/** Default constructor - 
	 * initializes with default color (black) and no points (empty polygon)
	 */
	public ColorPolygon(){
		super();
		this.color = Color.BLACK;
	}
	/** Constructor - with points without color.
	 * Initializes with default color (black) and set of points (vertices)
	 * @param xpoints int[] an array of X coordinates
	 * @param ypoints int[] an array of Y coordinates
	 * @param npoints int the total number of points in the Polygon
	 */
	public ColorPolygon(int[] xpoints, int[] ypoints, int npoints){
		super(xpoints, ypoints, npoints);
		this.color = Color.BLACK;
	}
	/** Constructor - copy of an existing Polygon. 
	 * Initializes with default color (black) and exact points from existing.
	 * @param p Polygon existing polygon to deep-copy
	 */
	public ColorPolygon(Polygon p){
		super(p.xpoints, p.ypoints, p.npoints);
		this.color = Color.BLACK;
	}
	/** Constructor - with color without points.
	 * Initializes with specified color and no points (empty polygon)
	 * @param color Color specified color of polygon
	 */
	public ColorPolygon(Color color){
		super();
		this.color = color;
	}
	/** Constructor - with color and points.
	 * Initializes with specified color and set of points (vertices)
	 * @param color Color specified color for polygon
	 * @param xpoints int[] an array of X coordinates
	 * @param ypoints int[] an array of Y coordinates
	 * @param npoints int the total number of points in the Polygon
	 */
	public ColorPolygon(Color color, int[] xpoints, int[] ypoints, int npoints){
		super(xpoints, ypoints, npoints);
		this.color = color;
	}
	/** Constructor -  copy of an existing Polygon with color. 
	 * Initializes with specified color and points from existing.
	 * @param color Color specifed color for polygon
	 * @param p Polygon existing polygon to copy
	 */
	public ColorPolygon(Color color, Polygon p){
		super(p.xpoints, p.ypoints, p.npoints);
		this.color = color;
	}
	/** Constructor - deep clone of an existing ColorPolygon. 
	 * Initializes with exact copy of color and points from existing.
	 * @param p ColorPolygon existing polygon to deep-copy
	 */
	public ColorPolygon(ColorPolygon p){
		super(p.xpoints, p.ypoints, p.npoints);
		this.color = p.color;
	}
	
	
	/** Getter to return color of ColorPolygon.
	 * @return Color value
	 */
	public Color getColor() {
		return color;
	}

	/** Setter to modify the color of this ColorPolygon
	 * @param color Color value to be set
	 */
	public void setColor(Color color) {
		this.color = color;
	}
	
	
	/** Cloneable interface method, creates a deep copy of
	 * this ColorPolygon object.
	 * @return ColorPolygon deep copy of this
	 * @throws CloneNotSupportedException
	 */
	protected Object clone() throws CloneNotSupportedException {
			return new ColorPolygon(this);
	}
	
}
