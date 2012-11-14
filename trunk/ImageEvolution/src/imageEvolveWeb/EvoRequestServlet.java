package imageEvolveWeb;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.UpdateCondition;
import com.amazonaws.services.sqs.AmazonSQSClient;

import imageEvolveWeb.SessionManagement;

/**
 * Servlet implementation class EvoReqServlet
 */
public class EvoRequestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	/** Source of randomness.
	 * ThreadLocal used to avoid blocking when used by
	 * concurrent threads
	 */
	private static final ThreadLocal<Random> rndSrc =
			new ThreadLocal <Random> () {
				@Override protected Random initialValue() { return new Random(); }
	};
	
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
		// request parameters
		String name = null;
		String description = null;
		File targetImage = null;
		
		// get current user
		Map<String,String> user = SessionManagement.getUser(request.getCookies());
		// check if can do request
		if (user.get("canEvoReguest").equals("true") && ServletFileUpload.isMultipartContent(request)){
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
			
			// get image Id and setup in simpleDB
			allocateImageName(name, description, user);
			
			// upload file to S3
			
			// issue request to SQS
			
			
		}
		
		
		
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.print("<html><body>");
		out.print("This is the EvoReqServlet!<br/>");
		out.print("</body></html>");
	}
	
	private static String allocateImageName(String name, String description, Map<String,String> user){
		boolean done = false;
		String imageId = null;
		while (!done){
			// randomly guess a new session id
			imageId = randomImageId(18)+"_o";
			String userId = user.get("itemName");
			String created = Long.toString((new Date()).getTime());
			//System.out.println("rndSessionId: "+tmp.sessionId);
			// create update request
			PutAttributesRequest putReq = new PutAttributesRequest();
			// set session parameters
			putReq = putReq.withDomainName("ImgEvo_images").withItemName(imageId);
			putReq = putReq.withAttributes(new ReplaceableAttribute("userId",
					userId,true));
			putReq = putReq.withAttributes(new ReplaceableAttribute("created",
					created,true));
			if (name!=null && !name.equals("")){
				putReq = putReq.withAttributes(new ReplaceableAttribute("name",name,true));
			}
			if (description!=null && !description.equals("")){
				putReq = putReq.withAttributes(new ReplaceableAttribute("description",
						description,true));
			}
			// set update conditions to prevent overwriting existing image
			putReq = putReq.withExpected(new UpdateCondition().withName("userId").withExists(false));
			putReq = putReq.withExpected(new UpdateCondition().withName("created").withExists(false));
			// make update
			sdb.get().putAttributes(putReq);
			// check update success
			GetAttributesResult existing = sdb.get().getAttributes(new GetAttributesRequest()
					.withDomainName("ImgEvo_images")
					.withItemName(imageId)
					.withAttributeNames("userId")
					.withAttributeNames("created")
					.withConsistentRead(true));
			done = !existing.getAttributes().isEmpty();
			for (Attribute a : existing.getAttributes()){
				if (a.getName().equals("userId")){
					done = (done) ? a.getValue().equals(userId) : false;
				} else if (a.getName().equals("created")){ 
					done = (done) ? a.getValue().equals(created) : false;
				}
			}
		}
		return imageId;
	}
	
	
	private static String randomImageId(int byteLength){
		// get some random bytes
		byte[] rndBytes = new byte[byteLength];
		rndSrc.get().nextBytes(rndBytes);
		// convert random bytes to hex
		byte[] b64Bytes = Base64.encodeBase64(rndBytes);
		// return base64 as string UTF-8
		// unfortunately commons codec 1.3 is missing the url-safe b64 variant
		// so i have to make that transformation here.
		try {
			return new String(b64Bytes, "UTF-8").replace('+', '-').replace('/', '_');
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	
	
	
	
}
