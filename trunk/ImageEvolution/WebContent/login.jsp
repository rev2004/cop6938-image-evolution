<%@page import="java.security.Key"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Login page</title>
	</head>
	<body>
		<div> 
			session id: <%= session.getId() %>
		</div>
		<div style="margin-left: 50px; margin-top: 40px; height: 60px;">
			<form action="loginServlet?identifier=https://www.google.com/accounts/o8/id" method="post"> 
				<input type="image" src="images/openid-logos-google.png" value=" " />
			</form>
			<form action="loginServlet?identifier=https://me.yahoo.com" method="post">
				<input type="image" src="images/openid-logos-yahoo.png" value=" " /> 
			</form>
		</div>		
	</body>
</html>