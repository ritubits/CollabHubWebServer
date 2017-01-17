package org.apache.collab.server.WebClient;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ConflictsBean {

	
	 String artifactName= null;
	 String conflictMessage= null;
	
	static Connection conn = LoadMySQLDriver.getConnection("localhost:3306");
	
	public  String getConflictMessages()
	{
		return conflictMessage;
	}
	
	public  String getArtifactNames()
	{
		return artifactName;
	}
	
	
	public  void getConflictMessageDetails()
	{
		Statement statement = null;
		  String sql = null;
		 ResultSet resultSet =null;
		artifactName=null;
		conflictMessage=null;
		
		  try {
	       if (conn !=null) 
	    	   { 
				statement = conn.createStatement();				    	   
	    	   // Result set get the result of the SQL query
				
				sql= "select table_name from INFORMATION_SCHEMA.tables where table_schema= 'collaborationhub' and table_name= 'conflictmessages';";
				resultSet = statement.executeQuery(sql);
				int count=resultSet.getRow();;

				if (count>0)
				{
				sql= "select * from conflictmessages";
				resultSet = statement.executeQuery(sql);
				while (resultSet.next())
				{
					if (artifactName == null)					
						artifactName = resultSet.getString("sentNode");
					else artifactName= artifactName+","+resultSet.getString("sentNode");
					
					
					if (conflictMessage == null)					
						conflictMessage = resultSet.getString("message");
					else conflictMessage= conflictMessage+","+resultSet.getString("message");
					
					
		
				}
	    	   resultSet.close();
				}
				else
				{
					artifactName = "No conflict messages";
					conflictMessage = "No conflict messages";
				}
	    	   }	       
		 } catch (SQLException e) {				
				e.printStackTrace();
			}	
	}
}

