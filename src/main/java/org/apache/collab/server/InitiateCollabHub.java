package org.apache.collab.server;


import javax.servlet.*;
import javax.servlet.http.*;

import java.io.IOException;


public class InitiateCollabHub extends HttpServlet{

	//this servlet will run a thread to clone the repository from GitHub
	// then connects to the DB and create the artifact table.
	//Later- graph
	
	String projectName =null;
    String ownerName= null;
    String ipAddSQL= null;
    String remoteURL= null;
    String tempPath= null;
    String srcPath= null;

     
    public void init(ServletConfig config) throws ServletException  
    {
    
    	//read parameters from web.xml
    	//read ipAddTomcat and ipAddSQL
    	super.init(config);
    	ipAddSQL = getServletContext().getInitParameter("ipAddSQL");
    }
    
	 public void doGet(HttpServletRequest request, HttpServletResponse response)
			    throws IOException, ServletException
			    {
		 
		 	response.setContentType("text/html");
	  
	        System.out.println(" In the InitiateCollabHub ");
	        projectName= request.getParameter("projectName");
	        ownerName = request.getParameter("ownerName");
	        remoteURL = request.getParameter("remoteURL"); 
	        tempPath = request.getParameter("tempPath"); 
	        srcPath = request.getParameter("srcPath");
	        
	        
	        System.out.println("projectName: "+projectName);
	        System.out.println("ownerName: "+ownerName);
	        System.out.println("remoteURL: "+remoteURL);
	        System.out.println("tempPath: "+tempPath);
	        System.out.println("srcPath: "+srcPath);

	        System.out.println("ipAddress SQL: "+ipAddSQL);
	        System.out.println("Before creating clone");
	        
	     
	        
	        CloneRemoteRepo clone= new CloneRemoteRepo(projectName, ipAddSQL,remoteURL, tempPath, srcPath);	     
	        Thread thread = new Thread(clone, "cloneThread");
	        thread.start();
	     
	        
	        ServletOutputStream outStream= response.getOutputStream();
	        outStream.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+
	        		"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<p><center>COG:: Project Graph Created</center>");    
	        
		  }//doGet 
	 
	
}
