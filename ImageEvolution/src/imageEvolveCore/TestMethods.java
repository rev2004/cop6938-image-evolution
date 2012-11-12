package imageEvolveCore;

/** Class for warehousing old test methods from ImgEvolution.
 * unit tests early versions of application parts.
 */
public class TestMethods {
	
	public static void main(String[] args){
		//renderTest();
		//fitnessTest();
		//polygonEditTest();
		//mutateTest();
		//gaussTest();
	}
	
	/* test rendering and outputting an image
	private static void renderTest(){
		EvoImg test = new EvoImg(100,100,4);
		test.dna[0] = new ColorPolygon();
		test.dna[0].color = Color.BLUE;
		test.dna[0].addPoint(50, 0);
		test.dna[0].addPoint(50, 50);
		test.dna[0].addPoint(0, 50);
		test.dna[1] = new ColorPolygon();
		test.dna[1].color = Color.GREEN;
		test.dna[1].addPoint(100, 0);
		test.dna[1].addPoint(100, 50);
		test.dna[1].addPoint(50, 50);
		test.dna[2] = new ColorPolygon();
		test.dna[2].color = Color.RED;
		test.dna[2].addPoint(50, 50);
		test.dna[2].addPoint(50, 100);
		test.dna[2].addPoint(0, 100);
		test.dna[3] = new ColorPolygon();
		test.dna[3].color = Color.ORANGE;
		test.dna[3].addPoint(100, 50);
		test.dna[3].addPoint(100, 100);
		test.dna[3].addPoint(50, 100);
		test.render();
		ImgEvolution.outputImage(test.image, "png", new File("t1.png"));
		BufferedImage t1 = getSourceImage(new File("t1.png"));
		ImgEvolution.outputImage(t1, "png", new File("t2.png"));
	} //*/
	
	/* test calculation of fitness for an image
	private static void fitnessTest(){
		// initialize
		Color c1 = new Color(0,0,0);
		Color c2 = new Color(255,255,255);
		Color c3 = new Color(0,0,255);
		EvoImg t1 = new EvoImg(99,99,4);
		EvoImg t2 = new EvoImg(99,99,4);
		// polygons
		t1.dna[0] = new ColorPolygon();
		t1.dna[0].addPoint(50, 0);
		t1.dna[0].addPoint(50, 50);
		t1.dna[0].addPoint(0, 50);
		t1.dna[1] = new ColorPolygon();
		t1.dna[1].addPoint(99, 0);
		t1.dna[1].addPoint(99, 50);
		t1.dna[1].addPoint(50, 50);
		t1.dna[2] = new ColorPolygon();
		t1.dna[2].addPoint(50, 50);
		t1.dna[2].addPoint(50, 99);
		t1.dna[2].addPoint(0, 99);
		t1.dna[3] = new ColorPolygon();
		t1.dna[3].addPoint(99, 50);
		t1.dna[3].addPoint(99, 99);
		t1.dna[3].addPoint(50, 99);
		t2.dna[0] = new ColorPolygon();
		t2.dna[0].addPoint(50, 0);
		t2.dna[0].addPoint(50, 50);
		t2.dna[0].addPoint(0, 50);
		t2.dna[1] = new ColorPolygon();
		t2.dna[1].addPoint(99, 0);
		t2.dna[1].addPoint(99, 50);
		t2.dna[1].addPoint(50, 50);
		t2.dna[2] = new ColorPolygon();
		t2.dna[2].addPoint(50, 50);
		t2.dna[2].addPoint(50, 99);
		t2.dna[2].addPoint(0, 99);
		t2.dna[3] = new ColorPolygon();
		t2.dna[3].addPoint(99, 50);
		t2.dna[3].addPoint(99, 99);
		t2.dna[3].addPoint(50, 99);
		// test 1
		t1.dna[0].color = Color.BLUE;
		t1.dna[1].color = Color.GREEN;
		t1.dna[2].color = Color.RED;
		t1.dna[3].color = Color.ORANGE;
		t2.dna[0].color = Color.BLUE;
		t2.dna[1].color = Color.GREEN;
		t2.dna[2].color = Color.RED;
		t2.dna[3].color = Color.ORANGE;
		t1.render();
		t2.render();
		t2.compare(t1.image);
		System.out.println("test2fitness="+t2.fitness+" expected="+((double)1-(double)0/8));
		// test 2
		t1.dna[0].color = c1;
		t2.dna[0].color = c2;
		t1.render();
		t2.render();
		t2.compare(t1.image);
		System.out.println("test2fitness="+t2.fitness+" expected="+((double)1-(double)1/8));
		// test 3
		t1.dna[1].color = c1;
		t2.dna[1].color = c2;
		t1.render();
		t2.render();
		t2.compare(t1.image);
		System.out.println("test2fitness="+t2.fitness+" expected="+((double)1-(double)2/8));
		// test 4
		t1.dna[2].color = c1;
		t2.dna[2].color = c2;
		t1.render();
		t2.render();
		t2.compare(t1.image);
		System.out.println("test2fitness="+t2.fitness+" expected="+((double)1-(double)3/8));
		// test 4
		t1.dna[3].color = c1;
		t2.dna[3].color = c2;
		t1.render();
		t2.render();
		t2.compare(t1.image);
		System.out.println("test2fitness="+t2.fitness+" expected="+((double)1-(double)4/8));
	} //*/
	
	/* test editing a polygon
	private static void polygonEditTest(){
		EvoImg test = new EvoImg(100,100,4);
		test.dna[0] = new ColorPolygon();
		test.dna[0].color = Color.BLUE;
		test.dna[0].addPoint(50, 0);
		test.dna[0].addPoint(50, 50);
		test.dna[0].addPoint(0, 50);
		test.dna[1] = new ColorPolygon();
		test.dna[1].color = Color.GREEN;
		test.dna[1].addPoint(100, 0);
		test.dna[1].addPoint(100, 50);
		test.dna[1].addPoint(50, 50);
		test.dna[2] = new ColorPolygon();
		test.dna[2].color = Color.RED;
		test.dna[2].addPoint(100, 0);
		test.dna[2].addPoint(50, 100);
		test.dna[2].addPoint(0, 100);
		test.dna[3] = new ColorPolygon();
		test.dna[3].color = Color.ORANGE;
		test.dna[3].addPoint(100, 50);
		test.dna[3].addPoint(100, 100);
		test.dna[3].addPoint(50, 100);
		test.render();
		ImgEvolution.outputImage(test.image, "png", new File("t1.png"));
		EvoImg test2 = test.copyDna();
		int[] xs = test.dna[3].xpoints;
		int[] ys = test.dna[3].ypoints;
		xs[0] = 0;
		ys[0] = 0;
		test.render();
		ImgEvolution.outputImage(test.image, "png", new File("t2.png"));
	} //*/
	
	/* test mutating an image
	private static void mutateTest(){
		EvoImg test = new EvoImg(100,100,4);
		test.dna[0] = new ColorPolygon();
		test.dna[0].color = Color.BLUE;
		test.dna[0].addPoint(50, 0);
		test.dna[0].addPoint(50, 50);
		test.dna[0].addPoint(0, 50);
		test.dna[1] = new ColorPolygon();
		test.dna[1].color = Color.GREEN;
		test.dna[1].addPoint(100, 0);
		test.dna[1].addPoint(100, 50);
		test.dna[1].addPoint(50, 50);
		test.dna[2] = new ColorPolygon();
		test.dna[2].color = Color.RED;
		test.dna[2].addPoint(50, 50);
		test.dna[2].addPoint(50, 100);
		test.dna[2].addPoint(0, 100);
		test.dna[3] = new ColorPolygon();
		test.dna[3].color = Color.ORANGE;
		test.dna[3].addPoint(100, 50);
		test.dna[3].addPoint(100, 100);
		test.dna[3].addPoint(50, 100);
		test.render();
		ImgEvolution.outputImage(test.image, "png", new File("t1.png"));
		EvoImg test2 = test.copyDna();
		for (int i=0; i<100; i++){	
			test2.mutateMedium();
			test2.render();
			ImgEvolution.outputImage(test2.image, "png", new File("t"+(i+2)+".png"));
		}
		System.out.println("mutation done");
	} //*/
	
	/* test gauss distribution
	private static void gaussTest(){
		int v1=5, v2=70, min=0, max=100; 
		for (int i=0; i<100; i++){
			v1=(int) EvoImg.gaussRange(v1, (double)max/2, min, max);
			v2=(int) EvoImg.gaussRange(v2, (double)max/2, min, max);
			System.out.println("v1="+v1+"\t\tv2="+v2);
		}
	} //*/
	
	/* test delta uniform distribution
	private static void delta Test(){
		int v1=5, v2=70, min=0, max=100; 
		for (int i=0; i<100; i++){
			v1=(int) EvoImg.deltaRange(v1, (double)max/4, min, max);
			v2=(int) EvoImg.deltaRange(v2, (double)max/4, min, max);
			System.out.println("v1="+v1+"\t\tv2="+v2);
		}
	} //*/
	
	
	
	
	
}
