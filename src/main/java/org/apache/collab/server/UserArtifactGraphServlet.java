package org.apache.collab.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.collab.server.Comparator.CompareGraphs;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

/**
 * This servlet is an important part of the UserActivityAnalyzer Component of
 * the COG server. It is invoked by the COGEclipseClient and gets invoked at
 * regular intervals. Once the collaboration is started, the user sends the file
 * contents from its Eclipse workspace. This servlet take the user file in
 * string and creates the client artifact graph an then invokes the comparator
 * program to compare the two graphs: project dependency graph with the server
 * and client artifact graph.
 * 
 * @author Ritu Arora
 * 
 */

public class UserArtifactGraphServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	String ipAddTomcat = null;
	String ipAddSQL = null;

	// private String DEST_PATH = "neo4jDB/Client/temp/";
	// private String SRC_PATH = "neo4jDB/Client/";

	String DEBUG = "FALSE";
	private static final String DB_PATH_SERVER = "neo4jDB/Server";
	GraphDatabaseService graphDbServer;

	/**
	 * Initializes the servlet with the required parameters from the web.xml
	 * file.
	 */
	public void init(ServletConfig config) throws ServletException {

		// read parameters from web.xml
		// read ipAddTomcat and ipAddSQL
		super.init(config);
		ipAddTomcat = getServletContext().getInitParameter("ipAddTomcat");
		ipAddSQL = getServletContext().getInitParameter("ipAddSQL");
		DEBUG = getServletContext().getInitParameter("DEBUG");

		File dbDirServer = new File(DB_PATH_SERVER);
		GraphDatabaseFactory graphFactoryServer = new GraphDatabaseFactory();
		GraphDatabaseBuilder graphBuilderServer = graphFactoryServer
				.newEmbeddedDatabaseBuilder(dbDirServer);
		graphDbServer = graphBuilderServer.newGraphDatabase();
		registerShutdownHook(graphDbServer);
	}

	/**
	 * Reads file content from the request object, converts to a string and
	 * passes it to an object of CreateUserArtifactGraph class to create the
	 * client artifact graph.
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String collabName = null;
		String fileName = null;
		String fileContent = null;

		if (DEBUG.equals("TRUE"))
			System.out.println(request.getParameter("text"));

		System.out.println(ServletFileUpload.isMultipartContent(request));

		System.out.println("In UserArtifactGraphServlet");

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

			// items = upload.parseRequest((RequestContext) request);
			FileItemIterator items = upload.getItemIterator(request);

			while (items.hasNext()) {
				FileItemStream item = items.next();

				if (item.isFormField()) {
					String name = item.getFieldName();
					String value = item.getName();

					System.out.println("Name:: " + name);
					System.out.println("Value:: " + value);

				} else {
					String fieldName = item.getFieldName();
					String name = item.getName();
					System.out.println("fieldName:: " + fieldName);
					fileName = parseFileName(name);
					System.out.println("fileName:: " + fileName);
					collabName = parseClientName(name);
					System.out.println("clientName:: " + collabName);

					// String contentType = item.getContentType();
					InputStream filecontent = item.openStream();

					BufferedReader reader = new BufferedReader(
							new InputStreamReader(filecontent));
					StringBuilder outBuilder = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						outBuilder.append(line);
						outBuilder.append('\n');
					}
					reader.close();
					fileContent = outBuilder.toString();

					if (DEBUG.equals("TRUE"))
						System.out.println(fileContent);

				}

			}
		} catch (FileUploadException e) {
			e.printStackTrace();
		}

		// writing content to file for remote viewing
		/*
		 * try { FileWriter writer = new
		 * FileWriter(SRC_PATH+collabName+"_artifact.txt", false);
		 * writer.write(fileContent);
		 * 
		 * writer.close(); } catch (IOException e) { e.printStackTrace(); }
		 * 
		 * File source = new File(SRC_PATH+collabName+"_artifact.txt"); File
		 * dest = new File(DEST_PATH+collabName+"_artifact.txt");
		 * 
		 * // copyToProjectRepo(source, dest);
		 * 
		 * copyFileUsingApacheCommonsIO(source,dest);
		 */

		CreateUserArtifactGraph userArtifactGraph = new CreateUserArtifactGraph(
				fileContent, fileName, collabName);
		userArtifactGraph.createGraph();

		CompareGraphs db = new CompareGraphs();
		db.initializeDB(collabName, ipAddSQL, graphDbServer);

	}

	public String parseFileName(String name) {
		name = name.substring(0, name.indexOf("|"));
		return name;

	}

	public String parseClientName(String name) {
		name = name.substring(name.indexOf("|") + 1, name.length());
		return name;

	}

	private void registerShutdownHook(final GraphDatabaseService graphDb) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				graphDb.shutdown();
			}
		});
	}

	public void destroy() {

		shutDown(graphDbServer);
	}

	void shutDown(GraphDatabaseService graphDb) {
		System.out.println("Shutting down database ...");
		graphDb.shutdown();
		System.out.println("DB server shuting down complete");
	}

	/*
	 * private void copyFileUsingApacheCommonsIO(File source, File dest) throws
	 * IOException { FileUtils.copyFile(source, dest); }
	 */
}
