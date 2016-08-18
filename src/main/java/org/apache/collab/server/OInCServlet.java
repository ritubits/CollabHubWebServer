package org.apache.collab.server;


import javax.servlet.*;
import javax.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Vector;

public class OInCServlet extends HttpServlet{
	

    String ipAddSQL= null;
	Connection con=null;
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
		    String collabName= null;
		    
		    Vector<String> activityTables= new Vector<String>();
		    
		 	response.setContentType("text/html");
	        PrintWriter out = response.getWriter();
	  
	        System.out.println(" In the EInCServlet ");       
	        collabName = request.getParameter("cName");
	
	        if (DEBUG.contains("TRUE")) System.out.println("ipAddress SQL: "+ipAddSQL);
	        
	        //make connection to DB
	      
	       //  con= LoadDriver.createConnection(ipAddSQL);//connect;
	         con= LoadDriver.connect;
	        String artifact=null;
	        String returnData = null;
		       if (con !=null) 
		    	   {
		    	   artifact=getEditArtifact(collabName);
		    	   returnData=getOInDC(artifact, activityTables);

		    	   System.out.println("EditArtifact:: "+artifact);
		    	   out.print(returnData);
		    	   }
		       else 
		       {
		    	 //  out.print("No Conflict Messages");
		    	   System.out.println("No connection exists:");//need to forward this error
		       }
		       //do not close this connection

	  	 
		  }//doGet 

	public String getEditArtifact(String collabName)
	 {
		String editArtifact=null; 
		Statement statement = null;
		  String sql = null;
		 ResultSet resultSet =null;
		  try {
		       if (con !=null) 
		    	   { 
		    			   
					statement = con.createStatement();				    	   
		    	   // Result set get the result of the SQL query
					sql= "select filename, activitytime from useractivity_"+collabName +" where activitytype='EDIT' and activitytime = (select max(activitytime) from useractivity_"+collabName+");";
					resultSet = statement.executeQuery(sql);
					while (resultSet.next())
					{
						
						editArtifact= resultSet.getString("filename");						
						//assumes only one row
					}
		    	   resultSet.close();
		    	   }	       
			 } catch (SQLException e) {				
					e.printStackTrace();
				}	
		return editArtifact;
	
	 }
	
	public String getOInDC(String artifact, Vector activityTables)
	{
		String EDCCollabData= null;
		
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
	   			       }
	   			      res.close();
	   			       
	   			      
	   			      
	   			      //get data from individual activity table
	   			   statement = con.createStatement();
	   			    String sql=null;  
	   			   Enumeration userActivityTableName= activityTables.elements();
	   			 while (userActivityTableName.hasMoreElements())
			    	  {
	   				 String usertableName= userActivityTableName.nextElement().toString();
	   				 	sql= "select filename from "+ usertableName+" where filename<>'"+artifact+"' and activitytype='OPEN' and MINUTE(activitytime) >= MINUTE(NOW()-INTERVAL 5 MINUTE);";
	   				 
	   				 	ResultSet resultSet = statement.executeQuery(sql);
	   				 
						while (resultSet.next())
						{
							
							if (EDCCollabData == null)
								EDCCollabData= parse(usertableName)+","+ resultSet.getString("filename");
							else EDCCollabData= EDCCollabData+"|" +parse(usertableName)+","+ resultSet.getString("filename");
							
						}
			    	   resultSet.close();
			    	  }
		}		   		 catch (Exception e) {
   			// TODO Auto-generated catch block
   			e.printStackTrace();
   		}
		return EDCCollabData;
	}
	
	
	public String parse(String tableName)
	{
		
		  int index= tableName.indexOf("_");
		   String s= tableName.substring(index+1, tableName.length());
		   
		return s;
	}
}
