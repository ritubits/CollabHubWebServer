package org.apache.collab.server;


import javax.servlet.*;
import javax.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class StopCollaborationServlet extends HttpServlet{

	//this servlet will connect to DB and 
	//DROP TABLE USERACTIVITY- LATER
	// remove from userdetails table the row with the collabname
	
	String projectName =null;
    String collabName= null;
    String ipAddTomcat= null;
    String ipAddSQL= null;
    PrintWriter out =null;
    static Connection con = null;
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
    
	 public void doGet(HttpServletRequest request, HttpServletResponse response)
			    throws IOException, ServletException
			    {
		 
		 	response.setContentType("text/html");
	 //       PrintWriter out = response.getWriter();
	  
		 	if (DEBUG.contains("TRUE")) System.out.println(" In the StopProjectServlet ");
	        projectName= request.getParameter("pName");
	        collabName = request.getParameter("cName");
	//        ipAddTomcat = request.getParameter("ipAddT");
	//        ipAddSQL = request.getParameter("ipAddSQL");
	        

	        if (DEBUG.contains("TRUE"))
	        {
	        	System.out.println("projectName: "+projectName);
	        System.out.println("collabName: "+collabName);
	        System.out.println("ipAddress Tomcat: "+ipAddTomcat);
	        System.out.println("ipAddress SQL: "+ipAddSQL);
	        }
	        
	       con= LoadDriver.connect;
	       if (con !=null) RemoveDatafromTable(con);
	       else 
	       {
	    	   System.out.println("No connection exists:");//need to forward this error
	       }
	       //do not close this connection
	  	 
		  }//doGet 
	 
	 public void RemoveDatafromTable(Connection conn)
	 {
		  Statement statement = null;

			 //insert data here
			 try {
		        	
				 if (DEBUG.contains("TRUE")) System.out.println("removing data from userDetails table in given database...");
				 statement = conn.createStatement();
			      
				 statement = conn.createStatement();
			      String sql = "DELETE FROM userdetails_"+projectName+
			                   "WHERE collabname = '"+collabName+"'";
			      if (DEBUG.contains("TRUE")) System.out.println(sql);
			      statement.executeUpdate(sql);
			   	   		            
			      if (DEBUG.contains("TRUE")) System.out.println("removed data from userDetails table in given database...");
			      
		          
		        } catch (SQLException ex) {
		            // handle any errors
		            System.out.println("SQLException: " + ex.getMessage());
		            System.out.println("SQLState: " + ex.getSQLState());
		            System.out.println("VendorError: " + ex.getErrorCode());
		            out.print("Exception:"+ex.getMessage());
		        }
		    	
	  	 }
}
