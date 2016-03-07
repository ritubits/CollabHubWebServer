package collaboration;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;


import java.io.File;

import java.io.IOException;

public class CollabInfoServlet extends HttpServlet{
	//gets the project name from the client
	// if project not exist- returns null
	// else return details of the collaborator
	
	// at present return all those names who have opened various artifacts 
	//returns following:
	// mike:g.java|geeta:k.java|sita:r.java|sita:g.java
	///
	
	String projectNamefromPlugin =null;
	String responseContent= null;
	File projectFile= null;
	Boolean exists= false;
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			    throws IOException, ServletException
			    {
			       
		
			projectNamefromPlugin= request.getParameter("pName");
			System.out.println(" In the CollabInfoServlet Servlet");
			System.out.println(projectNamefromPlugin);
			
		//	 File projectFile = new File("collabProject"+"\\"+projectNamefromPlugin+".txt");
		        
//		        if (projectFile.exists()) {
		        	//if project file does not exist
		        	//need to return null
		        	//no existing collaborators else do the following
		        	response.setContentType("text/html");
		        	PrintWriter out = response.getWriter();
					 
					  out.println(createDummyContent());
			        
			        
//		        	}      
        
			   //     readFromFile(out, "collabProject"+"\\"+"configProject.txt");
}//doGet

 
	 public void readFromFile(PrintWriter out, String pName)
	 {
		// send user details
		 
		// configFile format
		 // projectName|level|numberofmember|ipAddress
		 
		 BufferedReader br = null;
		
			try {
	 
				String sCurrentLine;
				File configFile = new File(pName);
			
			        
			        if (configFile.exists())
			        {
			        	// if file exists- then read and sent data
			        	// else need to send message- configure project first
			        	
	 
				br = new BufferedReader(new FileReader(configFile));
				
	 
				while ((sCurrentLine = br.readLine()) != null) {
					System.out.println(sCurrentLine);
					out.println(sCurrentLine);
				}
			}//try
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (br != null)br.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
	   
	 }// readFromFile
	 
	 public String createDummyContent()
	 {
		return ("mike:g.java|geeta:k.java|sita:r.java|sita:g.java");
	   
	 }//createDummyContent
	 
	 
}
