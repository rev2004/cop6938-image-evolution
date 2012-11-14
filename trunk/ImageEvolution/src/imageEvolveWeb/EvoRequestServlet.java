package imageEvolveWeb;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.sqs.AmazonSQSClient;

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
				AWSCredentials cred = new PropertiesCredentials(
						EvoRequestServlet.class.getClassLoader()
				        .getResourceAsStream("AwsCredentials.properties"));
				return new AmazonSimpleDBClient(cred);
			} catch (IOException e) {
				return null;
			}
		}
	};
	/** Simple Queue Service connection;
	 * Thread local used to prevent blocking when threads concurrent.
	 */
	private static final ThreadLocal<AmazonSQSClient> sqs = new ThreadLocal<AmazonSQSClient>() {
		@Override protected AmazonSQSClient initialValue() { 
			try {
				AWSCredentials cred = new PropertiesCredentials(
						EvoRequestServlet.class.getClassLoader()
				        .getResourceAsStream("AwsCredentials.properties"));
				return new AmazonSQSClient(cred);
			} catch (IOException e) {
				return null;
			}
		}
	};
	/** Simple Storage Service connection;
	 * Thread local used to prevent blocking when threads concurrent.
	 */
	private static final ThreadLocal<AmazonS3Client> s3 = new ThreadLocal<AmazonS3Client>() {
		@Override protected AmazonS3Client initialValue() { 
			try {
				AWSCredentials cred = new PropertiesCredentials(
						EvoRequestServlet.class.getClassLoader()
				        .getResourceAsStream("AwsCredentials.properties"));
				return new AmazonS3Client(cred);
			} catch (IOException e) {
				return null;
			}
		}
	};
	
    public EvoRequestServlet() {
        super();
    }
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// get current user
		Map<String,String> user = SessionManagement.getUser(request.getCookies());
		// parse request
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (isMultipart){
			// Create a factory for disk-based file items
			FileItemFactory factory = new DiskFileItemFactory();
			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);
			// Parse the request
			List<FileItem> items = null;
			try {
				@SuppressWarnings("unchecked")
				List<FileItem> tmp = upload.parseRequest(request);
				items = tmp;
			} catch (FileUploadException e) {
				e.printStackTrace();
			}
			// Process the uploaded items
			for(FileItem item : items) {
				if (item.isFormField()) {
					
					
					//processFormField(item);
					
					
				} else {
					
					
					//processUploadedFile(item);
					
					
				}
			}
		}
		
		
		
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.print("<html><body>");
		out.print("This is the EvoReqServlet!<br/>");
		out.print("</body></html>");
	}
	
}
