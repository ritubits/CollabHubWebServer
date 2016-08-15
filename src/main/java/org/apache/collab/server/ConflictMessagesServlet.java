package org.apache.collab.server;


import javax.servlet.*;
import javax.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ConflictMessagesServlet extends HttpServlet{
	

    String ipAddSQL= null;
    String collabName= null;
    String artifactName= null;
	 String conflictMessage= null;
	 
	 String activityArtifact=null;
	 String activityType=null;
	 Connection con=null;
    PrintWriter out =null;
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
		 
		 	response.setContentType("text/html");
	        PrintWriter out = response.getWriter();
	  
	        System.out.println(" In the ConflictMessagesServlet ");       
	        collabName = request.getParameter("cName");
	
	        if (DEBUG.contains("TRUE")) System.out.println("ipAddress SQL: "+ipAddSQL);
	        
	        //make connection to DB
	      
	         con= LoadDriver.connect;
	        String messages=null;
		       if (con !=null) 
		    	   {
		    	   getActivtyData(collabName);
		    	   messages=getConflictMessages(con);
		    	   System.out.println("ConflictMessageS:: "+messages);
		    	   out.print(messages);
		    	   }
		       else 
		       {
		    	   out.print("No Conflict Messages");
		    	   System.out.println("No connection exists:");//need to forward this error
		       }
		       //do not close this connection

	  	 
		  }//doGet 
    

		public  String getConflictMessages()
		{
			return conflictMessage;
		}
		
		public  String getArtifactNames()
		{
			return artifactName;
		}
		
		public  String getActivityArtifact()
		{
			return activityArtifact;
		}
		
		public  String getActivityType()
		{
			return activityType;
		}
		
	 public  void getActivtyData(String collabName)
		{
			Statement statement = null;
			  String sql = null;
			 ResultSet resultSet =null;
			 activityArtifact=null;
			activityType=null;
			
			  try {
		       if (con !=null) 
		    	   { 
					statement = con.createStatement();				    	   
		    	   // Result set get the result of the SQL query
					sql= "select filename, activitytype, max(activitytime) from (select * from useractivity_"+collabName+" order by filename ASC, activitytime DESC) as t2 group by filename;";
					resultSet = statement.executeQuery(sql);
					while (resultSet.next())
					{
						if (activityArtifact == null)					
							activityArtifact = resultSet.getString("filename");
						else activityArtifact= activityArtifact+","+resultSet.getString("filename");
						
						
						if (activityType == null)					
							activityType = resultSet.getString("activitytype");
						else activityType= activityType+","+resultSet.getString("activitytype");
						
						
			
					}
		    	   resultSet.close();

		    	   }	       
			 } catch (SQLException e) {				
					e.printStackTrace();
				}	
		}

	 public String getConflictMessages(Connection con)
	 {
		 Statement statement = null;
		  String sql = null;
		 ResultSet resultSet =null;
		artifactName=null;
		conflictMessage=null;
		
		  try {
	       if (con !=null) 
	    	   { 
	    	   String sentNode=null;
	    	   String message=null;
	    			   
				statement = con.createStatement();				    	   
	    	   // Result set get the result of the SQL query
				sql= "select * from conflictmessages where collabName <> '"+collabName+"'";
				resultSet = statement.executeQuery(sql);
				while (resultSet.next())
				{
					
					sentNode= resultSet.getString("sentNode");
					message= resultSet.getString("message");
					
					if (artifactFound(sentNode))
					{
					if (artifactName == null)					
						artifactName = sentNode;
					else artifactName= artifactName+","+sentNode;
					
					
					if (conflictMessage == null)					
						conflictMessage = message;
					else conflictMessage= conflictMessage+","+message;
					}
					
		
				}
	    	   resultSet.close();

	    	   }	       
		 } catch (SQLException e) {				
				e.printStackTrace();
			}	
		 return conflictMessage;
	 }
	 
		public boolean artifactFound(String artifactName)
		{
			boolean found=false;
		

			System.out.println("ActivityData:: "+getActivityArtifact());
			
			int index= artifactName.lastIndexOf('.');
			artifactName= artifactName.substring(index+1, artifactName.length());
			
			System.out.println("artifactName:: "+artifactName);
			
			if (getActivityArtifact() !=null)
				{
				if (getActivityArtifact().contains(artifactName)) found = true;
				}

			
			return found;
			
		}
}
