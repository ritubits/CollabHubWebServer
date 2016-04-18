package org.apache.collab.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class UserArtifactGraphServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	//this servlet take the user file in string and creates an artifact graph
	String collabName =null;
    String fileName= null;
    String fileContent= null;
    String ipAddTomcat= null;
    String ipAddSQL= null;
    PrintWriter out =null;
    String DEBUG=null;
    
    public void init(ServletConfig config) throws ServletException  
    {
    
    	//read parameters from web.xml
    	//read ipAddTomcat and ipAddSQL
    	super.init(config);
    	ipAddTomcat = getServletContext().getInitParameter("ipAddTomcat");
    	ipAddSQL = getServletContext().getInitParameter("ipAddSQL");
     	DEBUG = getServletContext().getInitParameter("DEBUG");
    }


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	 	response.setContentType("text/html");
  
	 	if (DEBUG.contains("TRUE")) System.out.println(" In the UserArtifactGraphServlet ");
	 	collabName= request.getParameter("collabName");
	 	fileName = request.getParameter("fileName");
	 	fileContent= request.getParameter("fileContent");

        if (DEBUG.contains("TRUE"))
        {	        	
        System.out.println("collabName: "+collabName);	        
        System.out.println("fileName: "+fileName);
        System.out.println("fileContent: "+fileContent);
        }
        
        CreateUserArtifactGraph userArtifactGraph= new CreateUserArtifactGraph(fileContent, fileName, collabName);
        userArtifactGraph.createGraph();
	}



}
