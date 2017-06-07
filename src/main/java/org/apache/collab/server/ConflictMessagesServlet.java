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
	

 //   String ipAddSQL= null;
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
//    	ipAddSQL = getServletContext().getInitParameter("ipAddSQL");
    	DEBUG = getServletContext().getInitParameter("DEBUG");
    }
    
	 public void doGet(HttpServletRequest request, HttpServletResponse response)
			    throws IOException, ServletException
			    {
		 
		 	response.setContentType("text/html");
	        PrintWriter out = response.getWriter();
	  
	        System.out.println(" In the ConflictMessagesServlet ");       
	        collabName = request.getParameter("cName");
	
//	        if (DEBUG.contains("TRUE")) System.out.println("ipAddress SQL: "+ipAddSQL);
	        
	        //make connection to DB
	      
	         con= LoadDriver.connect;
	        String messages=null;
		       if (con !=null) 
		    	   {
		    	   getActivtyData(collabName);
		    	   messages=getConflictMessages(con, collabName);
		    	   System.out.println("ConflictMessageS:: "+messages);
		    	   out.print((messages));
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

	 public String getConflictMessages(Connection con, String collabName)
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
	    		String color=null;	   
	    		String sentCollab= null;
				statement = con.createStatement();				    	   
	    	   // Result set get the result of the SQL query
				sql= "select * from conflictmessages where collabName <> '"+collabName+"' order by messagetime DESC";
				resultSet = statement.executeQuery(sql);
				while (resultSet.next())
				{
					
					sentNode= resultSet.getString("sentNode");
					message= resultSet.getString("message");
					sentCollab = resultSet.getString("collabName");//message sent by this collaborator
					if (artifactFound(sentNode))
					{
					if (artifactName == null)					
						artifactName = sentNode;
					else artifactName= artifactName+","+sentNode;
					
					//color= getColorString(sentNode);
					color= getCollabType(sentCollab);
						if (conflictMessage == null)					
							conflictMessage = "#"+color+message;
						else 
							{
								if (!conflictMessage.contains(message))
								{
									conflictMessage= conflictMessage+","+"#"+color+message;
								}
							}
					
					}
					
		
				}
	    	   resultSet.close();

	    	   }	       
		 } catch (SQLException e) {				
				e.printStackTrace();
			}	
		 return conflictMessage;
	 }
	 
	 public String getColorString(String sentNode)
	 {
		 String color=null;
		 String fName=null;
		 int index2=0;
		 int index= sentNode.lastIndexOf('.');
		 sentNode= sentNode.substring(index+1, sentNode.length());
		 //search 
		 Statement statement = null;
		  String sql = null;
		 ResultSet resultSet =null;
		String fileName=null;
		  try {
	       if (con !=null) 
	    	   { 
   
				statement = con.createStatement();				    	   
	    	   // Result set get the result of the SQL query
				 sql= "select filename, activitytype, activitytime from useractivity_"+collabName+ " where activitytype='EDIT' and activitytime= (select max(activitytime ) from useractivity_"+collabName+");";
				resultSet = statement.executeQuery(sql);
				while (resultSet.next())
				{
					fileName= resultSet.getString("filename");
					System.out.println("from getColorString fileName::"+fileName);
					index2= fileName.indexOf(".");
					 fName= fileName.substring(0, index2);
						System.out.println("from getColorString sentNode::"+sentNode);
						System.out.println("from getColorString fileName::"+fName);
					if (sentNode.contains(fName))
					{
						color="EDC";//"cyan";
						
					}
					else color= "EIC";//"yellow";
				}
	    	   }
		  }catch (SQLException e)
		  {
			  e.printStackTrace();
		  }
		  
		 return color;
			
	 }
	 
	 public String getCollabType(String sentCollab)
	 {
		 String color=null;
		 String myFileName=null;
		 String collabFileName=null;
		 
		 Statement statement = null;
		  String sql = null;
		 ResultSet resultSet =null;
		String fileName=null;
		  try {
	       if (con !=null) 
	    	   { 
   
				statement = con.createStatement();		
				sql= "SELECT useractivity_"+collabName+ "FROM information_schema.tables	WHERE table_schema = 'collaborationhub'	AND table_name = 'useractivity_"+collabName+";";
				resultSet = statement.executeQuery(sql);
				int num=0;
				while (resultSet.next())
				{
					resultSet.last();
					num= resultSet.getRow();
				}
	    	   resultSet.close();
	    	   
	    	   if (num >0)
	    	   {
		    	   // Result set get the result of the SQL query
					 sql= "select filename, activitytype, activitytime from useractivity_"+collabName+ " where activitytype='EDIT' and activitytime= (select max(activitytime ) from useractivity_"+collabName+");";
					resultSet = statement.executeQuery(sql);
					while (resultSet.next())
					{
						myFileName= resultSet.getString("filename");
					}
					resultSet.close();
				
				
					 sql= "select filename, activitytype, activitytime from useractivity_"+sentCollab+ " where activitytype='EDIT' and activitytime= (select max(activitytime ) from useractivity_"+sentCollab+");";
					resultSet = statement.executeQuery(sql);
					while (resultSet.next())
					{
						collabFileName= resultSet.getString("filename");
					}
					resultSet.close();
	    	   }
	    	   }
	       
	       if ((myFileName!=null) && (myFileName.equalsIgnoreCase(collabFileName)))
	       {
	    	   color="EDC";
	       }
	       else color="EIC";
	       
		  }catch (SQLException e)
		  {
			  e.printStackTrace();
		  }
		  
		 return color;
			
	 }
		public boolean artifactFound(String artifactName)
		{
			//This method checks if the sentNode is the same as the current edit artifact
			//Then returns true else false
			
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
