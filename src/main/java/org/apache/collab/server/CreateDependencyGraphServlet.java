package org.apache.collab.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class is used only for simulation/testing purposes.
 * 
 * @author Ritu Arora
 * 
 */
public class CreateDependencyGraphServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	// this servlet will invoke CreateDependencyGraph to create graph in server
	String projectName = null;
	String ownerName = null;
	String ipAddTomcat = null;
	String ipAddSQL = null;
	PrintWriter out = null;
	String DEBUG = null;

	public void init(ServletConfig config) throws ServletException {

		// read parameters from web.xml
		// read ipAddTomcat and ipAddSQL
		super.init(config);
		ipAddTomcat = getServletContext().getInitParameter("ipAddTomcat");
		ipAddSQL = getServletContext().getInitParameter("ipAddSQL");
		DEBUG = getServletContext().getInitParameter("DEBUG");
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		CreateDependencyGraph dpGraph = new CreateDependencyGraph();
		dpGraph.initializeDB("MathTutorialProject", "Path");
	}
}
