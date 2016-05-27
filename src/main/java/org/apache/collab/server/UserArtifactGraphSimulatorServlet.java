package org.apache.collab.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Random;

import org.apache.collab.server.Comparator.CompareGraphs;


public class UserArtifactGraphSimulatorServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    String fileName= null;
    String fileContent= null;
    String ipAddTomcat= null;
    String ipAddSQL= null;
    PrintWriter out =null;
    String DEBUG=null;
    
    long lEndTime;//System.currentTimeMillis();
	long difference;
	long lStartTime;
	
    public void init(ServletConfig config) throws ServletException  
    {
    
    	//read parameters from web.xml
    	//read ipAddTomcat and ipAddSQL
    	super.init(config);
    	ipAddTomcat = getServletContext().getInitParameter("ipAddTomcat");
    	ipAddSQL = getServletContext().getInitParameter("ipAddSQL");
     	DEBUG = getServletContext().getInitParameter("DEBUG");
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		int randomNumber;
	//	for (int i=0; i<5; i++)
	//	{
			readFileContent();
		//	System.out.println(fileContent);	
			fileName= "CloneRemoteRepo.java";
			
			lStartTime = System.currentTimeMillis();
			CreateUserArtifactGraph userArtifactGraph= new CreateUserArtifactGraph(fileContent, fileName, "CollabClient");
		    userArtifactGraph.createGraph();
		    
		    lEndTime = System.currentTimeMillis();
	    	difference = lEndTime - lStartTime;
	
	    	System.out.println("Elapsed milliseconds after creation of user Graph: " + difference);
	
		    CompareGraphs db= new CompareGraphs();
		//	db.initializeDB("CollabClient", ipAddSQL);
			
			//System.out.println(getRandomNumberInRange(20, 60));
		    lEndTime = System.currentTimeMillis();
	    	difference = lEndTime - lStartTime;
	    	
	    	System.out.println("total elapsed time: " + difference);
	    	
	/*		try {
				randomNumber= getRandomNumberInRange(20, 60);
				System.out.println("Random Number::" + randomNumber);
				Thread.sleep(1000*randomNumber);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			

	//	}
	}

	private  int getRandomNumberInRange(int min, int max) {

		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}

		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
	
	public void readFileContent()
	{
		BufferedReader br = null;
		fileContent=null;
		try {
 
			String sCurrentLine=null;
			br = new BufferedReader(new FileReader("D://CloneRemoteRepo.java"));
			while ((sCurrentLine = br.readLine()) != null) {
							
					
				if (fileContent == null)
					fileContent= sCurrentLine+"\n";
				else  fileContent= fileContent+ sCurrentLine+"\n";
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

}
