<%@ page language="java" session="true" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<%@ page import="imageEvolveWeb.*" %>
<%@ page import="org.apache.commons.codec.EncoderException" %>
<%@ page import="org.apache.commons.codec.net.URLCodec" %>

<%	// test getting SessionManagement cookie and session
	Map<String,String> user = SessionManagement.getUser(request.getCookies());
	// if user not validated, redirect to login
	if (user == null){
		// set the current url to be the return url
		String retUrl = request.getRequestURI();
		if (request.getQueryString()!=null
				&& !request.getQueryString().equals("")
		){
			retUrl += "?"+request.getQueryString();
		}
		// url encode the return url
		try {
			retUrl = (new URLCodec()).encode(retUrl);
		} catch (EncoderException e) {
			e.printStackTrace();
			retUrl = null;
		}
		// if return url valid, add to redirect to login page
		if (retUrl!=null && retUrl.length()>0){
			response.sendRedirect("login.jsp?retURL="+retUrl);
		} else {
			response.sendRedirect("login.jsp");
		}
	}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Image Evolution - Dashboard</title>
</head>
<body>
	<% if (user!=null) { %>
		<div style="padding:2px;border:1px solid red;"> <%= user.get("email") %></div>
		<div>
			Welcome, <%= user.get("friendlyName") %><br/>
		</div>
		<br/>
		<div> <span style="font-size:large;font-weight:bold;">User Info</span>
			<table style="border-width:1px;border-spacing:4px;border-style: solid;border-color: black;">
				<tr><td>User Id: </td><td><%= user.get("userId") %></td></tr>
				<tr><td>Email: </td><td><%= user.get("email") %></td></tr>
				<tr><td>Friendly Name: </td><td><%= user.get("friendlyName") %></td></tr>
				<tr><td>Evolve permission: </td><td><%= user.get("canEvoRequest") %></td></tr>
			</table>
		</div>
		<br/>
		<div>
			<% // get list of user's images
				List<Map<String,String>> myImages = ImageManagement.getUserImages(user.get("userId"));
			%>
			<table><tr>
				<td><span style="font-size:large;font-weight:bold;">Images</span></td>
				<td>
					<% // conditionally show new (request) image button
						if (user.get("canEvoRequest")!=null &&
							user.get("canEvoRequest").equals("true")
						) { %>
						<form action="request.jsp" method="get">
							<input type="submit" value="New Image" />
						</form>
					<% } %>
				</td>
				<!--<td><form action="img_list.jsp" method="get">
					<input type="submit" value="Show All" />
				</form></td>-->
			</tr></table>
			<table style="border-width:1px;border-spacing:4px;border-style: solid;border-color: black;">
				<% if (myImages!=null  && !myImages.isEmpty()) { %>
					<tr><th>Image Id</th><th>Name</th><th>Fitness</th></tr>
					<% for (Map<String,String> img : myImages) { %>
						<tr>
							<td>
								<a href="img_detail.jsp?id=<%= img.get("imgId") %>"><%= img.get("imgId") %></a>
							</td>
							<td><%= img.get("usr_name") %></td>
							<td><%= img.get("fitness") %></td>
						</tr>
					<% } %>
				<% } else { %>
					<tr><td>No Images to display</td></tr>
				<% } %>
			</table>
		</div>
	<% } %>
</body>
</html>