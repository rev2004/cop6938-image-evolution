package imageEvolveWeb;

import java.io.IOException;
import java.io.InputStream;
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
import com.amazonaws.services.s3.model.ObjectMetadata;
import imageEvolveWeb.SessionManagement;

/**
 * Servlet implementation class EvoReqServlet
 */
public class EvoRequestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
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
		InputStream targetImage = null;
		String targetType = null;
		long targetSize = 0;
		boolean success = true;
		
		// get current user
		Map<String,String> user = SessionManagement.getUser(request.getCookies());
		
		// check user permissions
		boolean permitted = user.containsKey("canEvoRequest") 
				&& user.get("canEvoRequest").equals("true");
		
		// check if can do request
		if (permitted && ServletFileUpload.isMultipartContent(request)){
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
				success=false;
			}
			// Process the uploaded items
			for(FileItem item : items) {
				if (item.isFormField()) {
					if(item.getFieldName().equals("name")){
						name = item.getString();
					} else if (item.getFieldName().equals("description")){
						description = item.getString();
					}
				} else {
					if(item.getFieldName().equals("file")){
						targetImage = item.getInputStream();
						targetType = item.getContentType();
						targetSize = item.getSize();
					}
				}
			}
		} else{
			System.out.println("request check failed -  permitted="+(permitted)+"  isMultipart"+(ServletFileUpload.isMultipartContent(request)));
			success=false;
		}
		
		// if a target provided and size is less than 1MiB
		if(targetImage!=null && targetSize>0 && targetSize<=1048576){
			// get image Id and setup in simpleDB
			String fileKey = StorageManagement.allocateImageId(null, null, user.get("itemName"), name, description)+"_o";
			// upload file to S3
			ObjectMetadata fileMeta = new ObjectMetadata();
			fileMeta.setContentType(targetType);
			fileMeta.setContentLength(targetSize);
			s3.get().putObject("ImgEvo", fileKey, targetImage, fileMeta);
			// issue request to SQS
			ReqQueueManagement sqsReq = new ReqQueueManagement();
			sqsReq.imageId = fileKey;
			sqsReq.targetId = fileKey;
			sqsReq.baseGen = null;
			sqsReq.fitThresh = 0.7;
			sqsReq.genThresh = 1000;
			sqsReq.strictThresh = false;
			sqsReq.sendSqsMsg();
			
		} else {
			System.out.println("targetImage check failed -  targ!=null"+(targetImage!=null)+"  size>0MiB"+(targetSize>0)+"  size<=1MiB:"+(targetSize<=1048576));
			success = false;
		}
		
		// display result to user
		if (success){
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.print("<html><body>");
			out.print("Your image evolution request was has been submitted.<br/>");
			out.print("</body></html>");
		} else if (!permitted){
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.print("<html><body>");
			out.print("Sorry. You do not have permission to submit image evolution requests.<br/>");
			out.print("</body></html>");
		} else {
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.print("<html><body>");
			out.print("There was an error that prevented your image evolution " +
					"request from being submitted. This is probably a bug.<br/>");
			out.print("</body></html>");
		}
	}
	

	
	
	
	
	
}
