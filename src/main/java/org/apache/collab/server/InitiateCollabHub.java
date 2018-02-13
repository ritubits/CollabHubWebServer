package org.apache.collab.server;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * This HttpServlet is the starting point of the COG tool. The user needs to
 * enter all the configuration parameters in the COG: Admin Module, which then
 * invokes this servlet. This servlet creates an object of CloneRemoteRepo and
 * invokes this thread to clone the remote repository.
 * 
 * @author Ritu Arora
 * @version 1.1
 * 
 */
public class InitiateCollabHub extends HttpServlet {

	private static final long serialVersionUID = 1L;
	String projectName = null;
	String ownerName = null;
	String ipAddSQL = null;
	String remoteURL = null;
	String tempPath = null;
	String srcPath = null;

	/**
	 * Reads MySQL DB IPAddress from the web.xml file and initializes the
	 * servlet
	 */
	public void init(ServletConfig config) throws ServletException {
		// read parameters from web.xml
		// read ipAddTomcat and ipAddSQL
		super.init(config);
		ipAddSQL = getServletContext().getInitParameter("ipAddSQL");
	}

	/**
	 * Invokes the CloneRemoteRepo thread for creating the clone of the remote
	 * repository.
	 * 
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		response.setContentType("text/html");

		System.out.println(" In the InitiateCollabHub ");
		projectName = request.getParameter("projectName");
		ownerName = request.getParameter("ownerName");
		remoteURL = request.getParameter("remoteURL");
		tempPath = request.getParameter("tempPath");
		srcPath = request.getParameter("srcPath");

		System.out.println("projectName: " + projectName);
		System.out.println("ownerName: " + ownerName);
		System.out.println("remoteURL: " + remoteURL);
		System.out.println("tempPath: " + tempPath);
		System.out.println("srcPath: " + srcPath);

		System.out.println("ipAddress SQL: " + ipAddSQL);
		System.out.println("Before creating clone");

		CloneRemoteRepo clone = new CloneRemoteRepo(projectName, ipAddSQL,
				remoteURL, tempPath, srcPath);
		Thread thread = new Thread(clone, "cloneThread");
		thread.start();

		ServletOutputStream outStream = response.getOutputStream();
		outStream
				.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
						+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<p><center>COG:: Project Graph Created</center>");

	}// doGet

}
