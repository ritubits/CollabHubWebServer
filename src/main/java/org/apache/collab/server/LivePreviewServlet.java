package org.apache.collab.server;


import javax.servlet.*;
import javax.servlet.http.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Vector;

public class LivePreviewServlet  extends HttpServlet{
	

    String ipAddSQL= null;
	Connection con=null;
    String DEBUG=null;

    
    public void init(ServletConfig config) throws ServletException  
    {
    
    	//read parameters from web.xml
    	//read ipAddTomcat and ipAddSQL
    	super.init(config);
    	ipAddSQL = getServletContext().getInitParameter("ipAddSQL");
    	DEBUG = getServletContext().getInitParameter("DEBUG");
    }
    
	 public void doGet(HttpServletRequest request, HttpServletResponse response)
			    throws IOException, ServletException
			    {
		    String collabName= null;
		    
		    response.setContentType("text/html");
	        PrintWriter out = response.getWriter();
	  
	        System.out.println(" In the LivePreviewServlet ");       
	        collabName = request.getParameter("cName");
	
	        if (DEBUG.contains("TRUE")) System.out.println("ipAddress SQL: "+ipAddSQL);
	        String content= readCollaboratorEditFile(collabName);
	        
	        out.print(content);
		      // System.out.println("Forwarding to LivePreview JSp");//need to forward this error
		     //  request.getRequestDispatcher("/LivePreview.jsp").forward(request,response);
		  }//doGet 

	 public String readCollaboratorEditFile(String collabName)
	 {
		BufferedReader br = null;
		FileReader fr = null;
		String content=null;
		String fileName="neo4jDB/Client/temp/"+collabName+"_artifact.txt";
				
		System.out.println("fileName::"+fileName);
			try {

				fr = new FileReader(fileName);
				BufferedReader reader = new BufferedReader(new FileReader(fileName));
		        StringBuilder outBuilder = new StringBuilder();
		        String line;
		        while ((line = reader.readLine()) != null) {
		        	
		        	outBuilder.append(line);
		        	outBuilder.append('\n');
		        	
		        }
		        reader.close();
		        content= outBuilder.toString();
			

			} catch (Exception e) {
				content= "Error";
				e.printStackTrace();

			} finally {

				try {

					if (br != null)
						br.close();

					if (fr != null)
						fr.close();

				} catch (IOException ex) {

					ex.printStackTrace();

				}

			}


	        
		System.out.println(content);
		 return content;
	 }
}
