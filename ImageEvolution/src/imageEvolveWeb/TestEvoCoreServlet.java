package imageEvolveWeb;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class TestEvoCoreServlet
 */
public class TestEvoCoreServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TestEvoCoreServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.print("<html><body>");
		out.print("TestEvoCoreServlet,<br/>");
		out.print(runTest(request.getParameter("test")));
		out.print("");
		out.print("</body></html>");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.print("<html><body>");
		out.print("TestEvoCoreServlet,<br/>");
		out.print(runTest(request.getParameter("test")));
		out.print("");
		out.print("</body></html>");
	}
	
	
	private static String runTest(String test){
		if(test.equals("create")){
			return testReqCreate();
		} else if(test.equals("receive")){
			return testRecvQueue();
		} else if(test.equals("evolve")){
			return testEvolution();
		} else if(test.equals("stuff")){
			return "stuff";
		}
		else {
			return "No Test matched";
		}
		
	}
	private static String testReqCreate(){
		ReqQueueManagement req = new ReqQueueManagement();
		req.imageId=ImageManagement.randomImageId(18);
		req.targetId= "canvas.png";
		req.baseGen = null;
		req.strictThresh=false;
		req.fitThresh = 0.75;
		req.genThresh = 10000;
		ReqQueueManagement.sendSqsMsg(req);
		return "testReqCreate";
	}
	private static String testRecvQueue(){
		ReqQueueManagement req = ReqQueueManagement.recvSqsMsg(20);
		return "testRecvQueue <br/>"+ req.createMsg().toString();
	}
	private static String testEvolution(){
		System.out.println("make EvolutionManager");
		EvolutionManager em = new EvolutionManager(2);
		System.out.println("run EvolutionManager");
		Thread t = new Thread(em);
		t.start();
		System.out.println("EvolutionManager running");
		return "testEvolution <br/>";
	}

}
