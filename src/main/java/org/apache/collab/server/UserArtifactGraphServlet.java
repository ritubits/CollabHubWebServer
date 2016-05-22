package org.apache.collab.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.apache.collab.server.Comparator.CompareGraphs;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.RequestContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;


public class UserArtifactGraphServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	
	//this servlet take the user file in string and creates an artifact graph
	String collabName =null;
    String fileName= null;
    String fileContent= null;
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

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		System.out.println(request.getParameter("text"));
		String partName = "text"; // or "data"
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		System.out.println(ServletFileUpload.isMultipartContent(request));
		

	    
	    try {
	 // Create a factory for disk-based file items
	    DiskFileItemFactory factory = new DiskFileItemFactory();

	    // Set factory constraints
	    factory.setSizeThreshold(4096);
	    factory.setRepository(new File("/client"));

	    // Create a new file upload handler
	    ServletFileUpload upload = new ServletFileUpload(factory);

	    // Set overall request size constraint
	    upload.setSizeMax(1000000);

		//	items = upload.parseRequest((RequestContext) request);
			FileItemIterator items = upload.getItemIterator(request);

	    while (items.hasNext()) {
	    	FileItemStream item = items.next();
	    
	        if (item.isFormField()) {
	        	String name = item.getFieldName();
	            String value = item.getName();
	            
	            System.out.println("Name:: "+name);
	            System.out.println("Value:: "+value);

	        } else {
	        	 String fieldName = item.getFieldName();
				    String name = item.getName();
				    System.out.println("fieldName:: "+fieldName);
				    fileName= parseFileName(name);
				    System.out.println("fileName:: "+fileName);
				    collabName= parseClientName(name);
				    System.out.println("clientName:: "+collabName);
				    
				    String contentType = item.getContentType();
				    InputStream filecontent = item.openStream();
				    
				    BufferedReader reader = new BufferedReader(new InputStreamReader(filecontent));
			        StringBuilder out = new StringBuilder();
			        String line;
			        while ((line = reader.readLine()) != null) {
			            out.append(line);
			            out.append('\n');
			        }
			        reader.close();
			        fileContent= out.toString();
			        
			    //    System.out.println(fileContent);  

	        }
	   
	       
	    	}
	    } catch (FileUploadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	/*    try {
            FileWriter writer = new FileWriter(fileName, true);
            writer.write(fileContent);
            
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
	    
	   CreateUserArtifactGraph userArtifactGraph= new CreateUserArtifactGraph(fileContent, fileName, collabName);
        userArtifactGraph.createGraph();
        
        CompareGraphs db= new CompareGraphs();
		 db.initializeDB(collabName, ipAddSQL);
		 
	}
	
	public String parseFileName(String name)
	{
		name= name.substring(0, name.indexOf("|"));
		return name;
		
	}
	
	public String parseClientName(String name)
	{
		name= name.substring(name.indexOf("|")+1, name.length() );
		return name;
		
	}
}
	



