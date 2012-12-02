package imageEvolveWeb;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;

/**
 * Servlet implementation class ImageServlet
 */
public class ImageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ImageServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request,response);

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request,response);
	}
	
	private void process(HttpServletRequest request, HttpServletResponse response){
		// get current user
		Map<String,String> user = SessionManagement.getUser(request.getCookies());
		
		// get parameters
		String verb = request.getParameter("verb");
		String imgId = request.getParameter("imgId");
		String retUrl = request.getParameter("retURL");
		// get image metadata (if exists)
		Map<String,String> imageMeta = null;
		if(imgId!=null && !imgId.equals("")){
			imageMeta = ImageManagement.getImgMetadata(imgId);
		}
		
		if(verb!=null){
			// delete option
			if(verb.equals("delete")){
				boolean success = false;
				boolean permitted = true;
				// user valid and image exists
				if(user!=null && imageMeta!=null){
					// check user rights
					if(user.get("userId")!=null && imageMeta.get("owner")!=null 
							&& user.get("userId").equals(imageMeta.get("owner"))
					){
						success = ImageManagement.deleteImage(imgId);
					} else {
						permitted = false;
					}
				}
				// output message to screen
				try {
					if(success){
						response.setContentType("text/html");
						PrintWriter out;
						out = response.getWriter();
						out.print("<html><body><form action=\"imageServlet?verb=return");
						if(retUrl!=null && !retUrl.equals("")){
							out.print("&retURL="+retUrl);
						}
						out.print("\" method=\"post\">");
						out.print("This image evolution has been deleted.<br/><br/>");
						out.print("<input type=\"submit\" value=\"Continue\" />");
						out.print("</form></body></html>");
					} else if(!permitted){
						response.setContentType("text/html");
						PrintWriter out;
						out = response.getWriter();
						out.print("<html><body><form action=\"imageServlet?verb=return");
						if(retUrl!=null && !retUrl.equals("")){
							out.print("&retURL="+retUrl);
						}
						out.print("\" method=\"post\">");
						out.print("You do not have permission to delete this image. Please contact image owner.<br/><br/>");
						out.print("<input type=\"submit\" value=\"Continue\" />");
						out.print("</form></body></html>");
					} else {
						response.setContentType("text/html");
						PrintWriter out;
						out = response.getWriter();
						out.print("<html><body><form action=\"imageServlet?verb=return");
						if(retUrl!=null && !retUrl.equals("")){
							out.print("&retURL="+retUrl);
						}
						out.print("\" method=\"post\">");
						out.print("There was an error while deleting this image.<br/><br/>");
						out.print("<input type=\"submit\" value=\"Continue\" />");
						out.print("</form></body></html>");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			// return (redirect) option
			else if(verb.equals("return")){
				// redirect to desired page (default is dashboard)
				if(retUrl!=null && !retUrl.equals("")){
					try {
						response.sendRedirect((new URLCodec()).decode(retUrl));
					} catch (IOException e) {
						e.printStackTrace();
					} catch (DecoderException e) {
						e.printStackTrace();
					}
				} else {
					try {
						response.sendRedirect("dashboard.jsp");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			// else verb not recognized, do nothing
		}
		// else no verb, do nothing
	}
}
