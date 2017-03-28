package org.apache.collab.server.WebClient;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class LivePreviewBean {

	 String projectName= null;
	 String collabName= null;
	 String artifactName= null;
	 
	 public void setCollabName(String cName)
	 {
		 //collabName="DevA";
		 collabName=cName;
	 }
	 
	 public void setProjectName(String pName)
	 {
		 projectName=pName;
	 }
	 
	 public void setArtifactName(String aName)
	 {
		 artifactName=aName;
	 }
	 
	 public String readCollaboratorEditFile()
	 {
		 
		BufferedReader br = null;
		FileReader fr = null;
		String content=null;
		String fileName="neo4jDB/Client/temp/"+collabName+"_artifact.txt";
				
		System.out.println("fileName::"+fileName);
			try {

				fr = new FileReader(fileName);
				String sCurrentLine;

				   
				BufferedReader reader = new BufferedReader(new FileReader(fileName));
		        StringBuilder outBuilder = new StringBuilder();
		        String line;
		        while ((line = reader.readLine()) != null) {
		        	outBuilder.append(line);
		        	outBuilder.append('\n');
		        }
		        reader.close();
		        content= outBuilder.toString();
			

			} catch (Exception e) {

				e.printStackTrace();

			} finally {

				try {

					if (br != null)
						br.close();

					if (fr != null)
						fr.close();

				} catch (IOException ex) {

					ex.printStackTrace();

				}

			}


	        
		System.out.println(content);
		 return content;
	 }
}
