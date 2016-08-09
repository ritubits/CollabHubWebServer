package org.apache.collab.server;


import javax.servlet.*;
import javax.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.PrintWriter;
public class DeRegisterProjectServlet extends HttpServlet{

	//this servlet will connect to DB and drop the regProject_projectName table
	String projectName =null;
    String ownerName= null;
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
    
	 public void doGet(HttpServletRequest request, HttpServletResponse response)
			    throws IOException, ServletException
			    {
		 
		 	response.setContentType("text/html");
		 	   out = response.getWriter();
		 	
		 			
	        if (DEBUG.contains("TRUE")) System.out.println(" In the DeRegisterProjectServlet ");
	        projectName= request.getParameter("pName");
	        ownerName = request.getParameter("oName");
	        //ipAddTomcat = request.getParameter("ipAddT");
	        //ipAddSQL = request.getParameter("ipAddSQL");
	        
	        if (DEBUG.contains("TRUE")) System.out.println("projectName: "+projectName);
	        if (DEBUG.contains("TRUE")) System.out.println("ownerName: "+ownerName);
	        if (DEBUG.contains("TRUE")) System.out.println("ipAddress Tomcat: "+ipAddTomcat);
	        if (DEBUG.contains("TRUE")) System.out.println("ipAddress SQL: "+ipAddSQL);
	        
	        //make connection to DB
	        //drop table regproject_projectName
	        // columns: ProjectName, OwnerName and Tomcat_IP
	       Connection conn= LoadDriver.createConnection(ipAddSQL);
	       dropTableRegProject(conn);
	      
	       	  	 
		  }//doGet 
	 
		 
	 public void dropTableRegProject(Connection conn)
	 {
		  Statement statement = null;
	  
		 //create table here
		 try {
	        	
			  String sql = "DROP TABLE IF EXISTS regProject";
			  if (DEBUG.contains("TRUE"))  System.out.println("SQL to drop table: "+ sql);
			  statement = conn.createStatement();
			  statement.executeUpdate(sql);
			  if (DEBUG.contains("TRUE"))  System.out.println("Table  deleted in given database..."); 
	          
			  sql = "DROP TABLE IF EXISTS conflictmessages";
			  if (DEBUG.contains("TRUE"))  System.out.println("SQL to drop table: "+ sql);
			  statement = conn.createStatement();
			  statement.executeUpdate(sql);
			  if (DEBUG.contains("TRUE"))  System.out.println("Table  deleted in given database..."); 
			  
			  sql = "DROP TABLE IF EXISTS userdetails_"+projectName+";";
			  if (DEBUG.contains("TRUE"))  System.out.println("SQL to drop table: "+ sql);
			  statement = conn.createStatement();
			  statement.executeUpdate(sql);
			  if (DEBUG.contains("TRUE"))  System.out.println("Table  deleted in given database..."); 
			  
			 
	        } catch (SQLException ex) {
	            // handle any errors
	            System.out.println("SQLException: " + ex.getMessage());
	            System.out.println("SQLState: " + ex.getSQLState());
	            System.out.println("VendorError: " + ex.getErrorCode());
	            out.print("Exception:"+ex.getMessage());
	        }
		 finally{
			 LoadDriver.closeConnection();
		 }
	    	
	 }
}
