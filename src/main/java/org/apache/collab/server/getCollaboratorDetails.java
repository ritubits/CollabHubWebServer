package org.apache.collab.server;


import javax.servlet.*;
import javax.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Vector;


public class getCollaboratorDetails extends HttpServlet{

	//this servlet will connect to DB and obtains user activity data
	String projectName =null;
    String ipAddTomcat= null;
    String ipAddSQL= null;
    PrintWriter out =null;
    String DEBUG=null;
    
    ResultSet rs=null;
    String sql =null;
    // vector to store names of all activity tables
    static Vector<String> activityTables= new Vector<String>();
    
    //vector to store all artifact names
    static Vector<String> artifactList= new Vector<String>();
    String activityString = null;
    
    Connection con=null;
    
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
	  
	        if (DEBUG.contains("TRUE")) System.out.println(" In the getCollaboratorDetails ");
	     //   projectName= request.getParameter("pName");
	        

	        if (DEBUG.contains("TRUE"))
	        {
	     //   System.out.println("projectName: "+projectName);;
	        System.out.println("ipAddress Tomcat: "+ipAddTomcat);
	        System.out.println("ipAddress SQL: "+ipAddSQL);
	        }
	        //make connection to DB
	        //does not use the existing connection	 
	        try {
	     	   con= LoadDriver.createConnection(ipAddSQL);
	     	   projectName= getProjectName(con);
		       String out = getDataUserActivity(con);
		  	 	PrintWriter outWriter = response.getWriter();
		  	 	
		  	  if (DEBUG.contains("TRUE")) System.out.println(out);
		  	 	
		  	 	outWriter.print(out);
			//	con.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  }//doGet 
	 
	 public String getProjectName(Connection con)
	 {
		 String pName=null;
		  try
		  {
	       Statement statement = con.createStatement();
		      
			 String sql = "SELECT distinct(projectName) FROM regproject";
			 ResultSet rs = statement.executeQuery(sql);
		
		      while(rs.next()){
		    	  pName = rs.getString("projectName");
		      }
		     
		      rs.close();
		  }
		  catch (Exception e)
		  {
			  e.printStackTrace();
		  }
		 return pName;
		 
	 }
	 
	 
	 public String getDataUserActivity(Connection conn)
	 {
		 String out = null;
		 out= startProcessing(conn);
		 
		 activityTables.removeAllElements();
		 artifactList.removeAllElements();
		 return out;
		 
				 
	 }
	 
	  public void InsertActivityCollaboratorTable(String artifactName, int noOfCollab, Connection con)
	  {
		  //insert in to table no of collaborators value
		  Statement statement = null;
		  String sql = null;

			 //insert data here
			 try {
		        	
			
				 statement = con.createStatement();
				
				 			 
				 sql = "INSERT INTO artifactCollab_"+projectName+
		                   " (filename, noCollab) VALUES  ('"+artifactName+"',"+noOfCollab+")";
				 
				 if (DEBUG.contains("TRUE"))  System.out.println("SQL: "+sql);
				 statement.executeUpdate(sql);
			 }catch(Exception e)
			 {
				 e.printStackTrace();
			 }
			   	   	
	  }
	  
	  public String createOutputString(Connection con)
	  {
		  //select from activity_Collaborator table
		  //create out string
		  //first.java:1|second.java:3|
		  //need response option
		  //creating string
		  String out= null;
		  String fileName=null;
		  int no= 0;
		  try
		  {
	       Statement statement = con.createStatement();
		      
			 String sql = "SELECT * FROM artifactCollab_"+projectName;
			 ResultSet rs = statement.executeQuery(sql);
		
		      while(rs.next()){
		    	fileName = rs.getString("fileName");
		    	no = rs.getInt("noCollab");
		    	
		    	if (out == null)
		    		out = fileName+":"+no+"|";
		    	else out = out+ fileName+":"+no+"|";
		      }
		     
		      rs.close();
		  }
		  catch (Exception e)
		  {
			  e.printStackTrace();
		  }
		  System.out.print(out);
		  return out;
	  }
	  
		 public void createArtifactCollaboratorTable(Connection conn)
		 {
			  Statement statement = null;
		  
			 //create table here
			 try {
		        	
				 if (DEBUG.contains("TRUE")) System.out.println("Creating table artifact_Collaborator...");
				 statement = conn.createStatement();
			      
			      String sql = "CREATE TABLE IF NOT EXISTS artifactCollab_"+projectName+
			                   "(filename VARCHAR(30) not NULL, " +
			                   " noCollab INTEGER )";

			                    
			      if (DEBUG.contains("TRUE"))  System.out.println("SQL: "+sql);

			      statement.executeUpdate(sql);
		      
		          
		        } catch (SQLException ex) {
		            // handle any errors
		            System.out.println("SQLException: " + ex.getMessage());
		            System.out.println("SQLState: " + ex.getSQLState());
		            System.out.println("VendorError: " + ex.getErrorCode());

		        }
		    	
		 }
		 
		 public String startProcessing(Connection con)
		 {
			  String outputString= null;
		    	try {

		   	       java.sql.DatabaseMetaData meta;
		   		
		   			meta = con.getMetaData();
		   			String tableName = null;
		   			Statement statement =null;
		   			 ResultSet res = meta.getTables(null, null, null, new String[] {"TABLE"});
		   			 if (DEBUG.contains("TRUE"))  System.out.println("List of tables: "); 
		   			       while (res.next()) {
		   			    	   
		   			    	   tableName = res.getString("TABLE_NAME");
		   			    	   if (tableName.contains("useractivity"))
		   			    	   {
		   			    		   //add to table array for activities
		   			    		   activityTables.addElement(tableName);
		   			    		 if (DEBUG.contains("TRUE")) System.out.println("UserActivity tables:: "+ tableName);
		   			    	   }
		   			    	   
		   			    	 if (tableName.contains("artifactcollab"))
		   			    	   {
		   			    		   //artifactCollab Table exists
		   			    		 //delete * from it
		   			    		statement = con.createStatement();
		   			             String sql = "DELETE FROM "+tableName;
		   			          if (DEBUG.contains("TRUE")) System.out.println("Deleting from "+ tableName);             
		   			          statement.executeUpdate(sql);	

		   			    	   }
		   			       }
		   			       res.close();
		   			       
		   			       
		   			       //get all artifacts from artifact table
		   			       statement = con.createStatement();
		   				      
		   					 String sql = "SELECT filename FROM artifact_"+projectName;
		   					 ResultSet rs = statement.executeQuery(sql);
		   				     
		   				      while(rs.next()){
		   				    	  artifactList.addElement(rs.getString("filename"));
		   				    	 if (DEBUG.contains("TRUE"))  System.out.println(rs.getString("filename"));

		   				      }
		   				      rs.close();
		   				      
		   				      createArtifactCollaboratorTable(con);
		   				      
		   				      // loop - for each artifact- get whether entry exist in each useractivity table
		   				      Enumeration artifactEnum = artifactList.elements();
		   				      String artifactName= null;
		   				      int noOfCollab =0;
		   				      while (artifactEnum.hasMoreElements())
		   				      {
		   				    	  artifactName= (String) artifactEnum.nextElement();
		   				    	  Enumeration userActivityName= activityTables.elements();
		   				    	  String userActivityTableName= null;
		   				    	  int count=0;
		   				    	  noOfCollab =0;
		   				    	  while (userActivityName.hasMoreElements())
		   				    	  {
		   				    		
		   				    		  userActivityTableName= (String) userActivityName.nextElement();
		   					    	  sql= "SELECT Count(fileName) as total FROM "+userActivityTableName+" where fileName='"+artifactName+"';";
		   					    	 if (DEBUG.contains("TRUE"))  System.out.println("SQL:: Select:: "+sql);
		   					    	  rs = statement.executeQuery(sql);
		   							     
		   						      while(rs.next()){
		   						    	count = rs.getInt("total");
		   						    	
		   						    	//if count >0, that means collaborator exists for the file
		   						    	// increment corresponding collab number by 1;
		   						    	if (count>0) noOfCollab++;
		   						      }
		   					      rs.close();
		   				    	  }
		   				    	  //update table with number of collab
		   				    	  
		   				    	  InsertActivityCollaboratorTable(artifactName,noOfCollab, con);
		   				    	  
		   				      }
		   				    
		   				   outputString=  createOutputString(con);
		   			       //con.close();
		   		} catch (Exception e) {
		   			// TODO Auto-generated catch block
		   			e.printStackTrace();
		   		}
		    	return outputString;
		 }
		 
		  public void destroy() {
			    try {
			    	//drop table if exists
			    	//DROP TABLE IF EXISTS AgentDetail
			     
			    	if (con !=null) 
			    	{
			    		Statement statement = con.createStatement();
			    		
			    		 if (DEBUG.contains("TRUE")) System.out.println("drop table table artifact_Collaborator...");
					 statement = con.createStatement();
				      
				      String sql = "DROP TABLE IF EXISTS artifactCollab_"+projectName;
				                    
				      if (DEBUG.contains("TRUE")) System.out.println("SQL: "+sql);

				      statement.executeUpdate(sql);
				      
					con.close();
			    		}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			  }
}
