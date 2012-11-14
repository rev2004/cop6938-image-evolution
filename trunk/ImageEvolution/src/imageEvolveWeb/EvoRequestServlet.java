package imageEvolveWeb;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;

import imageEvolveWeb.SessionManagement;

/**
 * Servlet implementation class EvoReqServlet
 */
public class EvoRequestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	/** SimpleDB connection;
	 * Thread local used to prevent blocking when threads concurrent.
	 */
	private static final ThreadLocal<AmazonSimpleDBClient> sdb = new ThreadLocal<AmazonSimpleDBClient>() {
		@Override protected AmazonSimpleDBClient initialValue() { 
			try {
				AWSCredentials cred;
				cred = new PropertiesCredentials(
				        SessionManagement.class.getClassLoader()
				        .getResourceAsStream("AwsCredentials.properties"));
				return new AmazonSimpleDBClient(cred);
			} catch (IOException e) {
				return null;
			}
		}
	};
	
	
	
	
	
	
	
    public EvoRequestServlet() {
        super();
    }
    
    
    
    
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.print("<html><body>");
		out.print("This is the EvoReqServlet!<br/>");
		out.print("</body></html>");
	}
	
}
