package org.apache.collab.server.WebClient;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.collab.server.LoadDriver;

public class CollaborationBean {
	 String projectName= null;
	 String ownerName= null;
	
	static Connection conn = LoadMySQLDriver.getConnection("localhost:3306");
	
	
	public  String getOwnerName()
	{
		return ownerName;
	}
	public  String getProjectDetails()
	{
	
		Statement statement = null;
		  String sql = null;
		 ResultSet resultSet =null;
		
		  try {
	       if (conn !=null) 
	    	   { 
				statement = conn.createStatement();				    	   
	    	   // Result set get the result of the SQL query
				sql= "select table_name from INFORMATION_SCHEMA.tables where table_schema= 'collaborationhub' and table_name= 'regProject';";
				resultSet = statement.executeQuery(sql);
				int count=resultSet.getRow();;

				if (count>0)
				{
					sql= "select projectName, ownerName from regProject";// assumes only one row exists
					while (resultSet.next())
					{
						projectName = resultSet.getString("projectName");
						ownerName = resultSet.getString("ownerName");					
					}
		    	   resultSet.close();
				}
	    	   }	       
		 } catch (SQLException e) {				
				e.printStackTrace();
			}

		  return projectName;
	
	}
	
	
	public  String getUserDetails()
	{
		String collabNames= null;
		Statement statement = null;
		  String sql = null;
		 ResultSet resultSet =null;
		
		  try {
	       if (conn !=null) 
	    	   { 
				statement = conn.createStatement();				    	   
	    	   // Result set get the result of the SQL query
				sql= "select collabName from userdetails_"+projectName;
				resultSet = statement.executeQuery(sql);
				while (resultSet.next())
				{
					if (collabNames == null)					
					collabNames = resultSet.getString("collabName");
					else collabNames= collabNames+","+resultSet.getString("collabName");
		
				}
	    	   resultSet.close();

	    	   }	       
		 } catch (SQLException e) {				
				e.printStackTrace();
			}	
		return collabNames;
	}
	
/*	public static void main(String args[])
	{
		System.out.println("ProjectName::"+getProjectDetails());
		System.out.println("OwnerName::"+getOwnerName());
		System.out.println("CollabName::"+getUserDetails());
	}*/
}
