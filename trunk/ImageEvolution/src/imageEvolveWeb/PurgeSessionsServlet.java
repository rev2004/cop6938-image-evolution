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
    
	private static AmazonSimpleDBClient sdb = null;
	/** Get the SimpleDb connect (initialize if not already existing)
	 * keeps multiple instances of connection from being created
	 */
	private static AmazonSimpleDBClient getSDB(){
		if (sdb==null){ 
			AWSCredentials cred;
			try {
				cred = new PropertiesCredentials(
				        SessionManagement.class.getClassLoader().getResourceAsStream("AwsCredentials.properties"));
				sdb = new AmazonSimpleDBClient(cred);
			} catch (IOException e) {
				sdb = null;
			}
		}
		return sdb;
	}
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PurgeSessionsServlet() {
        super();
    }

    private static void purgeSessions(){
    	String sessionDomainName = "ImgEvo_sessions";
		AmazonSimpleDBClient sdb_loc = getSDB();
		DeleteDomainRequest delete = new DeleteDomainRequest();
		delete.setDomainName(sessionDomainName);
		sdb_loc.deleteDomain(delete);
		CreateDomainRequest create = new CreateDomainRequest();
		create.setDomainName(sessionDomainName);
		sdb_loc.createDomain(create);
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
