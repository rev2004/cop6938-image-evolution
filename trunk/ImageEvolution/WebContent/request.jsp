<%@ page language="java" session="true" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.Map" %>
<%@ page import="imageEvolveWeb.*" %>
<%@ page import="org.apache.commons.codec.EncoderException" %>
<%@ page import="org.apache.commons.codec.net.URLCodec" %>

<%	// test getting SessionManagement cookie and session
	Map<String,String> user = SessionManagement.getUser(request.getCookies());
	// if user not validated, redirect to login
	if (user == null){
		// set the current url to be the return url
		String retUrl = request.getRequestURI();
		try {
			retUrl = (new URLCodec()).encode(retUrl);
		} catch (EncoderException e) {
			e.printStackTrace();
			retUrl = null;
		}
		// if return url valid, add to redirect to login page
		if (retUrl!=null && retUrl.length()>0){
			response.sendRedirect("login.jsp?retURL="+request.getRequestURI()+"?"+request.getQueryString());
		} else {
			response.sendRedirect("login.jsp");
		}
	}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Image Evolution - Evolution Request</title>
</head>
<body>
	
	<% if (user!=null) { %>
		<div style="padding:2px;border:1px solid red;"> <%= user.get("email") %></div>
    <% } %>
    
	<div>
		Please select the file you would like to use as a target for evolution.<br/>
		You may also provide a name and description for this image to help you organize your collection.
	</div><br/>
	<form action="evoRequestServlet" method="post" enctype="multipart/form-data">
		<table>
		<tr><td style="text-align:right">name:&nbsp;</td><td><input type="text" name="name" /></td></tr>
		<tr><td style="text-align:right">description:&nbsp;</td><td><input type="text" name="description" /></td></tr>
		<tr><td style="text-align:right">file:&nbsp;</td><td><input type="file" name="file" /></td></tr>
		<tr><td>&nbsp;</td><td style="padding-top:5px;"><input type="submit" value="Submit" /></td></tr>
		</table>
	</form>
</body>
</html>