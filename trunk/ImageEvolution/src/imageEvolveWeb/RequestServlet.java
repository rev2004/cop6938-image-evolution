package imageEvolveWeb;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
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
public class RequestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	/** Simple Storage Service connection;
	 * Thread local used to prevent blocking when threads concurrent.
	 */
	private static final ThreadLocal<AmazonS3Client> s3 = new ThreadLocal<AmazonS3Client>() {
		@Override protected AmazonS3Client initialValue() { 
			try {
				AWSCredentials cred = new PropertiesCredentials(
						RequestServlet.class.getClassLoader()
				        .getResourceAsStream("AwsCredentials.properties"));
				return new AmazonS3Client(cred);
			} catch (IOException e) {
				return null;
			}
		}
	};
	
    public RequestServlet() {
        super();
    }
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// request parameters
		String name = null;
		String description = null;
		InputStream targetImage = null;
		String targetType = null;
		long targetSize = 0;
		double fitness = 0.0;
		int generations = 0;
		boolean strict = false;
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
				if(item.getFieldName().equals("name")){
					name = item.getString();
				} else if (item.getFieldName().equals("description")){
					description = item.getString();
				}
				if(item.getFieldName().equals("file")){
					targetImage = item.getInputStream();
					targetType = item.getContentType();
					targetSize = item.getSize();
				}
				else if (item.getFieldName().equals("fitness")){
					try {
						fitness = Double.parseDouble(item.getString());
					} catch (NumberFormatException e) {
						success = false;
					}
				}
				else if (item.getFieldName().equals("generations")){
					try {
						generations = Integer.parseInt(item.getString());
					} catch (NumberFormatException e) {
						success = false;
					}
				}
				else if (item.getFieldName().equals("strict")){
					if (item.getString()!=null 
							&& item.getString().equalsIgnoreCase("strict")){
						strict = true;
					} else if (Boolean.parseBoolean(item.getString())){
						strict = true;
					}
				}
			}
		} else {
			success=false;
		}
		// if previous steps successful and a target provided and size is less than 1MiB
		if(success && targetImage!=null && targetSize>0 && targetSize<=1048576){
			// get image Id and setup in simpleDB
			String imgKey = ImageManagement.allocateImageId(null, user.get("itemName"));
			String fileKey = imgKey+"_o";
			Map<String,String> attributes = new HashMap<String,String>();
			attributes.put("usr_name", name);
			attributes.put("usr_description", description);
			//attributes.put("targetId", null);
			ImageManagement.setImgMetadata(imgKey, attributes);
			// upload file to S3
			ObjectMetadata fileMeta = new ObjectMetadata();
			fileMeta.setContentType(targetType);
			fileMeta.setContentLength(targetSize);
			s3.get().putObject("ImgEvo", fileKey, targetImage, fileMeta);
			// issue request to SQS
			RequestManagement sqsReq = new RequestManagement();
			sqsReq.imageId = imgKey;
			sqsReq.targetId = fileKey;
			sqsReq.baseGen = null;
			sqsReq.fitThresh = fitness;
			sqsReq.genThresh = generations;
			sqsReq.strictThresh = strict;
			RequestManagement.sendSqsMsg(sqsReq);
		} else {
			success = false;
		}
		// display result to user
		if (success){
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.print("<html><body><form action=\"dashboard.jsp\" method=\"get\">");
			out.print("Your image evolution request has been submitted.<br/><br/>");
			out.print("<input type=\"submit\" value=\"Return to dashboard\" />");
			out.print("</form></body></html>");
		} else if (!permitted){
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.print("<html><body><form action=\"dashboard.jsp\" method=\"get\">");
			out.print("Sorry.<br>"
					+"You do not have permission to submit image evolution requests.<br/><br/>");
			out.print("<input type=\"submit\" value=\"Return to dashboard\" />");
			out.print("</form></body></html>");
		} else {
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.print("<html><body><form action=\"dashboard.jsp\" method=\"get\">");
			out.print("There was an error that prevented your image evolution "
					+"request from being submitted. This is probably a bug.<br/><br/>");
			out.print("<input type=\"submit\" value=\"Return to dashboard\" />");
			out.print("</form></body></html>");
		}
	}
}
