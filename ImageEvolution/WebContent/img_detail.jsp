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

<%
	// get image metadata
	String imgKey = request.getParameter("id");
	Map<String,String> image = ImageManagement.getImgMetadata(imgKey);
%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	
	<% if (user!=null) { %>
		<div style="padding:2px;border:1px solid red;"> <%= user.get("email") %></div>
    <% } %>
	
	<% if (image!=null && user!=null
			&& image.get("owner")!=null 
			&& user.get("userId")!=null
			&& image.get("owner").equals(user.get("userId"))
		){ 
		String origImgKey = (image.get("targetId")!=null) ? image.get("targetId") : imgKey+"_o";
		String evoImgKey = imgKey+"_r";
	%>
		<table>
			<tr><td>
				<img alt="Original Image" src="
					<%= (ImageManagement.imageExists(origImgKey))? 
						ImageManagement.getImageUrl(origImgKey, 600000):"images/no-image.png"
					%>">
			</td>
			<td>
				<img alt="Evolved Image" src="
					<%= (ImageManagement.imageExists(evoImgKey))? 
						ImageManagement.getImageUrl(evoImgKey, 600000):"images/processing_blue.gif"
					%>">
			</td></tr>
			<tr><td>
				Original Image
			</td>
			<td>
				Evolved Image
			</td></tr>
		</table>
		<br/>
		<table>
			<tr><td>name</td><td>image.get("usr_name")</td></tr>
			<tr><td>description</td><td>image.get("usr_description")</td></tr>
			<tr><td>fitness</td><td>image.get("fitness")</td></tr>
			<tr><td>generations</td><td>image.get("generations")</td></tr>
		</table>
    <% } else { %>
		<div>The specified image is not available to be viewed.</div>
	<% } %>
	
</body>
</html>