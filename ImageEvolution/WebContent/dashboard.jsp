<%@ page language="java" session="true" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.Map" %>
<%@ page import="imageEvolveWeb.*" %>

<%	// test getting SessionManagement cookie and session
	Map<String,String> user= SessionManagement.getUser(request.getCookies());
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Image Evolution - Dashboard</title>
</head>
<body>
	<div style="padding:2px;border:1px solid red;">userName= <%= user.get("email") %></div>
	<div>Welcome, <%= user.get("friendlyName") %></div>
</body>
</html>