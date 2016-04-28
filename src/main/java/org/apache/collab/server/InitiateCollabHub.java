package org.apache.collab.server;


import javax.servlet.*;
import javax.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;

public class InitiateCollabHub extends HttpServlet{

	//this servlet will run a thread to clone the repository from GitHub
	// then connects to the DB and create the artifact table.
	//Later- graph
	
	String projectName =null;
    String ownerName= null;
    String ipAddTomcat= null;
    String ipAddSQL= null;
    PrintWriter out =null;
     
    public void init(ServletConfig config) throws ServletException  
    {
    
    	//read parameters from web.xml
    	//read ipAddTomcat and ipAddSQL
    	super.init(config);
    	ipAddTomcat = getServletContext().getInitParameter("ipAddTomcat");
    	ipAddSQL = getServletContext().getInitParameter("ipAddSQL");
    }
    
	 public void doGet(HttpServletRequest request, HttpServletResponse response)
			    throws IOException, ServletException
			    {
		 
		 	response.setContentType("text/html");
	 //       PrintWriter out = response.getWriter();
	  
	        System.out.println(" In the InitiateCollabHub ");
	        projectName= request.getParameter("pName");
	        ownerName = request.getParameter("oName");

	        System.out.println("projectName: "+projectName);
	        System.out.println("ownerName: "+ownerName);
	        System.out.println("ipAddress Tomcat: "+ipAddTomcat);
	        System.out.println("ipAddress SQL: "+ipAddSQL);
	        System.out.println("Before creating clone");
	        
	     
	        
	        CloneRemoteRepo clone= new CloneRemoteRepo(projectName, ipAddSQL);	     
	        Thread thread = new Thread(clone, "cloneThread");
	        thread.start();
	     
	        

		  }//doGet 
	 
	
}
