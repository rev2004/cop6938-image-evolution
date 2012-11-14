package imageEvolveWeb;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;

/**
 * Servlet implementation class purgeSessionsServlet
 */
public class PurgeSessionsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	/** SimpleDB connection;
	 * Thread local used to prevent blocking when threads concurrent.
	 */
	private static final ThreadLocal<AmazonSimpleDBClient> sdb = new ThreadLocal<AmazonSimpleDBClient>() {
		@Override protected AmazonSimpleDBClient initialValue() { 
			try {
				AWSCredentials cred = new PropertiesCredentials(
						EvoRequestServlet.class.getClassLoader()
				        .getResourceAsStream("AwsCredentials.properties"));
				return new AmazonSimpleDBClient(cred);
			} catch (IOException e) {
				return null;
			}
		}
	};
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PurgeSessionsServlet() {
        super();
    }

    private static void purgeSessions(){
    	String sessionDomainName = "ImgEvo_sessions";
		DeleteDomainRequest delete = new DeleteDomainRequest();
		delete.setDomainName(sessionDomainName);
		sdb.get().deleteDomain(delete);
		CreateDomainRequest create = new CreateDomainRequest();
		create.setDomainName(sessionDomainName);
		sdb.get().createDomain(create);
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		purgeSessions();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		purgeSessions();
	}

}
