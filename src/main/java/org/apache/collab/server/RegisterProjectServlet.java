package org.apache.collab.server;


import javax.servlet.*;
import javax.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
//import java.io.PrintWriter;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RegisterProjectServlet extends HttpServlet{

	//this servlet will connect to DB and create the regProject_projectName table
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
	 //       PrintWriter out = response.getWriter();
	  
		 	if (DEBUG.contains("TRUE")) System.out.println(" In the RegisterProjectServlet ");
	        projectName= request.getParameter("pName");
	        ownerName = request.getParameter("oName");
//	        ipAddTomcat = request.getParameter("ipAddT");
//	        ipAddSQL = request.getParameter("ipAddSQL");
	        
	        //check if the configProject file exists
	        //if yes, append, else create and append
	        if (DEBUG.contains("TRUE"))
	        {	        	
	        System.out.println("projectName: "+projectName);	        
	        System.out.println("ownerName: "+ownerName);
	        System.out.println("ipAddress Tomcat: "+ipAddTomcat);
	        System.out.println("ipAddress SQL: "+ipAddSQL);
	        }
	        //make connection to DB
	        //create table regproject_projectName
	        // columns: ProjectName, OwnerName and Tomcat_IP
	       Connection con= LoadDriver.createConnection(ipAddSQL);
	       createTableRegProject(con);
	       addDatatoTable(con);
	       createTableUserDetails(con);
	       
	       //create a table for storing messages, if not already exist
	       createTableConflictMessages(con); // with Project Name
	       LoadDriver.closeConnection();
		  }//doGet 
	 
	 public void addDatatoTable(Connection conn)
	 {
		  Statement statement = null;

			 //insert data here
			 try {
		        	
				 if (DEBUG.contains("TRUE")) System.out.println("inserting data in table in given database...");
				 statement = conn.createStatement();
			      
				 String sql = "INSERT INTO regProject_"+projectName+
		                   " VALUES ('"+projectName+"','"+ownerName+"','"+ipAddTomcat+"');";
				 if (DEBUG.contains("TRUE")) System.out.println("SQL: "+sql);
				 statement.executeUpdate(sql);
			   	   		            
				 if (DEBUG.contains("TRUE"))  System.out.println("inserted data...");
			      
		          
		        } catch (SQLException ex) {
		            // handle any errors
		            System.out.println("SQLException: " + ex.getMessage());
		            System.out.println("SQLState: " + ex.getSQLState());
		            System.out.println("VendorError: " + ex.getErrorCode());
		            out.print("Exception:"+ex.getMessage());
		        }
		    	
	  	 }
	 
	 public void createTableRegProject(Connection conn)
	 {
		  Statement statement = null;
	  
		 //create table here
		 try {
	        	
			 if (DEBUG.contains("TRUE")) System.out.println("Creating table in given database...");
			 statement = conn.createStatement();
		      
		      String sql = "CREATE TABLE regProject_"+projectName+
		                   "(projectname VARCHAR(30) not NULL, " +
		                   " ownerName VARCHAR(30), " + 
		                   " ipAddTomcat VARCHAR(30), " + 
		                   " PRIMARY KEY ( projectname ))"; 
		      if (DEBUG.contains("TRUE")) System.out.println("SQL: "+sql);

		      statement.executeUpdate(sql);
		      if (DEBUG.contains("TRUE")) System.out.println("Created table in given database...");
		      
	          
	        } catch (SQLException ex) {
	            // handle any errors
	            System.out.println("SQLException: " + ex.getMessage());
	            System.out.println("SQLState: " + ex.getSQLState());
	            System.out.println("VendorError: " + ex.getErrorCode());
	            out.print("Exception:"+ex.getMessage());
	        }
	    	
	 }
	 
	 public void createTableUserDetails(Connection conn)
	 {
		  Statement statement = null;
	  
		 //create table here
		 try {
	        	
			 if (DEBUG.contains("TRUE")) System.out.println("Creating table user details in given database...");
			 statement = conn.createStatement();
		      
		      String sql = "CREATE TABLE userdetails_"+projectName+
		                   "(projectname VARCHAR(30) not NULL, " +
		                   " collabName VARCHAR(30), " + 
		                   " action VARCHAR(30))"; 
		      if (DEBUG.contains("TRUE")) System.out.println("SQL: "+sql);

		      statement.executeUpdate(sql);
		      if (DEBUG.contains("TRUE")) System.out.println("Created table user details in given database...");
		      
	          
	        } catch (SQLException ex) {
	            // handle any errors
	            System.out.println("SQLException: " + ex.getMessage());
	            System.out.println("SQLState: " + ex.getSQLState());
	            System.out.println("VendorError: " + ex.getErrorCode());
	            out.print("Exception:"+ex.getMessage());
	        }
	    	
	 }
	 
	 public void createTableConflictMessages(Connection conn)
	 {
		  Statement statement = null;
	  
		 //create table here
		 try {
	        	
			 if (DEBUG.contains("TRUE")) System.out.println("Creating conflictMessages table in given database...");
			 statement = conn.createStatement();
		      
		      String sql = "CREATE TABLE IF NOT EXISTS conflictMessages"+
		                   "(sentNode VARCHAR(30) not NULL, " +
		                   " message VARCHAR(200), " + 
		                   " messagetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP) "; 
		      
		      if (DEBUG.contains("TRUE")) System.out.println("SQL: "+sql);

		      statement.executeUpdate(sql);
		      if (DEBUG.contains("TRUE")) System.out.println("Created table in given database...");
		      
	          
	        } catch (SQLException ex) {
	            // handle any errors
	            System.out.println("SQLException: " + ex.getMessage());
	            System.out.println("SQLState: " + ex.getSQLState());
	            System.out.println("VendorError: " + ex.getErrorCode());
	            out.print("Exception:"+ex.getMessage());
	        }
	    	
	 }
}
