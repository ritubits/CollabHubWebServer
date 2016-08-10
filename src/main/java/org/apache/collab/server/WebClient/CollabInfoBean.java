package org.apache.collab.server.WebClient;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CollabInfoBean {

	
	 String artifactName= null;
	 String conflictMessage= null;
	 
	 String activityArtifact=null;
	 String activityType=null;
	
	static Connection conn = LoadMySQLDriver.getConnection("localhost:3306");
	
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
	       if (conn !=null) 
	    	   { 
				statement = conn.createStatement();				    	   
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
	
	public  void getConflictMessageDetails(String collabName)
	{
		Statement statement = null;
		  String sql = null;
		 ResultSet resultSet =null;
		artifactName=null;
		conflictMessage=null;
		
		  try {
	       if (conn !=null) 
	    	   { 
	    	   String sentNode=null;
	    	   String message=null;
	    			   
				statement = conn.createStatement();				    	   
	    	   // Result set get the result of the SQL query
				sql= "select * from conflictmessages";
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
	}
	
	public boolean artifactFound(String artifactName)
	{
		boolean found=false;
	

		System.out.println("ActivityData:: "+getActivityArtifact());
		
		int index= artifactName.lastIndexOf('.');
		artifactName= artifactName.substring(index+1, artifactName.length());
		
		System.out.println("artifactName:: "+artifactName);
		
		if (getActivityArtifact().contains(artifactName)) found = true;

		
		return found;
		
	}
}

