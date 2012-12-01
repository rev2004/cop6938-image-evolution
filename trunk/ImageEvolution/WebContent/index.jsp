<%@ page language="java" session="true" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="java.util.Map" %>
<%@ page import="imageEvolveWeb.*" %>
<%@ page import="com.amazonaws.*" %>
<%@ page import="com.amazonaws.auth.*" %>
<%@ page import="com.amazonaws.services.ec2.*" %>
<%@ page import="com.amazonaws.services.ec2.model.*" %>
<%@ page import="com.amazonaws.services.s3.*" %>
<%@ page import="com.amazonaws.services.s3.model.*" %>
<%@ page import="com.amazonaws.services.simpledb.*" %>
<%@ page import="com.amazonaws.services.simpledb.model.*" %>
<%@ page import="org.apache.commons.codec.EncoderException" %>
<%@ page import="org.apache.commons.codec.net.URLCodec" %>



<%! // Share the client objects across threads to
    // avoid creating new clients for each web request
    private AmazonEC2      ec2;
    private AmazonS3        s3;
    private AmazonSimpleDB sdb;
 %>

<%
    /*
     * AWS Elastic Beanstalk checks your application's health by periodically
     * sending an HTTP HEAD request to a resource in your application. By
     * default, this is the root or default resource in your application,
     * but can be configured for each environment.
     *
     * Here, we report success as long as the app server is up, but skip
     * generating the whole page since this is a HEAD request only. You
     * can employ more sophisticated health checks in your application.
     */
    if (request.getMethod().equals("HEAD")) return;
%>

<%
    if (ec2 == null) {
        AWSCredentials credentials = new PropertiesCredentials(
            getClass().getClassLoader().getResourceAsStream("AwsCredentials.properties"));
        ec2 = new AmazonEC2Client(credentials);
        s3  = new AmazonS3Client(credentials);
        sdb = new AmazonSimpleDBClient(credentials);
    }
%>

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

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8">
    <title>Test/Example Page</title>
    <link rel="stylesheet" href="styles/styles.css" type="text/css" media="screen">
</head>
<body>
	
	<% if (user!=null) { %>
		<div style="padding:2px;border:1px solid red;"> <%= user.get("email") %></div>
    <% } %>
	
	<div id="page intro">
		Hello <%= (user!=null)?user.get("friendlyName"):"null" %>!
	</div>
	
    <div id="content" class="container">
        <div class="section grid grid5 s3">
            <h2>Amazon S3 Buckets:</h2>
            <ul>
            <% for (Bucket bucket : s3.listBuckets()) { %>
               <li> <%= bucket.getName() %> </li>
            <% } %>
            </ul>
        </div>

        <div class="section grid grid5 sdb">
            <h2>Amazon SimpleDB Domains:</h2>
            <ul>
            <% for (String domainName : sdb.listDomains().getDomainNames()) { %>
               <li> <%= domainName %></li>
            <% } %>
            </ul>
        </div>

        <div class="section grid grid5 gridlast ec2">
            <h2>Amazon EC2 Instances:</h2>
            <ul>
            <% for (Reservation reservation : ec2.describeInstances().getReservations()) { %>
                <% for (Instance instance : reservation.getInstances()) { %>
                   <li> <%= instance.getInstanceId() %></li>
                <% } %>
            <% } %>
            </ul>
        </div>
    </div>
</body>
</html>