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
		 String sql1 = null;
		 ResultSet resultSet1 =null;
		
		  try {
	       if (conn !=null) 
	    	   { 
				statement = conn.createStatement();				    	   
	    	   // Result set get the result of the SQL query
				sql1= "select table_name from INFORMATION_SCHEMA.tables where table_schema= 'collaborationhub' and table_name= 'regProject';";
				resultSet1 = statement.executeQuery(sql1);
				resultSet1.last();
				int count=resultSet1.getRow();
				System.out.println("Count from CollaborayionBean::"+count);
				
				if (count>0)
				{
					sql= "select projectName, ownerName from regProject";// assumes only one row exists
					resultSet = statement.executeQuery(sql);
					while (resultSet.next())
					{
						projectName = resultSet.getString("projectName");
						System.out.println("ProjectName from CollaborayionBean::"+projectName);
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
				
				sql= "select table_name from INFORMATION_SCHEMA.tables where table_schema= 'collaborationhub' and table_name= 'userdetails_"+projectName+"';";
				resultSet = statement.executeQuery(sql);
				resultSet.last();
				int count=resultSet.getRow();;

				if (count>0)
				{
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
				else
				{
					collabNames = "No data to display";
				}
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
