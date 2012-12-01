<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="java.security.Key"%>
<%@ page import="org.apache.commons.codec.EncoderException" %>
<%@ page import="org.apache.commons.codec.net.URLCodec" %>
<%@ page import="javax.servlet.http.Cookie" %>
    
<%
	// add 
	String retUrl = request.getParameter("retURL");
	if (retUrl!=null && retUrl.length()>0){
		Cookie retCookie = new Cookie("retURL",(new URLCodec()).decode(retUrl));
		retCookie.setMaxAge(5*60);
		response.addCookie(retCookie);
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Login page</title>
	</head>
	<body>
		<div>
			You must login to use this site.<br/>
			Please select one of the following methods to login.
		</div>
		<div style="margin-left: 50px; margin-top: 40px; height: 60px;">
			<form action="loginServlet?&identifier=https://www.google.com/accounts/o8/id" method="post"> 
				<input type="image" src="images/openid-logos-google.png" value=" " />
			</form>
			<!-- <form action="loginServlet?&identifier=https://me.yahoo.com" method="post">
				<input type="image" src="images/openid-logos-noyahoo.png" value=" " /> 
			</form> -->
			<img src="images/openid-logos-noyahoo.png" />
		</div>		
	</body>
</html>