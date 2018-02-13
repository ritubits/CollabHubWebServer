package org.apache.collab.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet is invoked by the COGEclipseClient in order to find the number
 * of collaborators allowed to collaborator over a registered project.
 * 
 * @author Ritu Arora
 * 
 */
public class AllowedCollaboratorServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	String projectName = null;
	String collabName = null;
	String ipAddTomcat = null;
	String ipAddSQL = null;
	PrintWriter out = null;
	Connection con = null;
	String DEBUG = null;

	public void init(ServletConfig config) throws ServletException {
		// read parameters from web.xml
		// read ipAddTomcat and ipAddSQL
		super.init(config);
		ipAddTomcat = getServletContext().getInitParameter("ipAddTomcat");
		ipAddSQL = getServletContext().getInitParameter("ipAddSQL");
		DEBUG = getServletContext().getInitParameter("DEBUG");
		System.out.println("In AllowedCollaboratorServlet");
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		System.out.println("In AllowedCollaboratorServlet::doGet");
		if (DEBUG.contains("TRUE"))
			System.out.println(" In the StartCollaborationServlet ");
		projectName = request.getParameter("pName");
		collabName = request.getParameter("cName");

		if (DEBUG.contains("TRUE")) {
			System.out.println("projectName: " + projectName);
			System.out.println("collabName: " + collabName);
			System.out.println("ipAddress Tomcat: " + ipAddTomcat);
			System.out.println("ipAddress SQL: " + ipAddSQL);
		}

		// make connection to DB
		con = LoadDriver.createConnection(ipAddSQL);
		int allowedCollab = getAllowedCollaborators(con);
		System.out.println("Allowed No::" + allowedCollab);

		int currentCollab = getCurrentCollaborators(con);

		if (currentCollab < allowedCollab) {
			// return true
			out.print("true");
		} else {
			// return false
			out.print("false");
		}

	}// doGet

	public int getAllowedCollaborators(Connection con) {
		System.out
				.println("In AllowedCollaboratorServlet::getAllowedCollaborators");
		int allowedNo = 0;
		Statement statement = null;
		String sql = null;
		ResultSet resultSet = null;
		try {
			if (con != null) {
				statement = con.createStatement();
				// Result set get the result of the SQL query
				sql = "select allowedCollab from regproject where projectName ='"
						+ projectName + "'";
				resultSet = statement.executeQuery(sql);
				while (resultSet.next()) {
					allowedNo = resultSet.getInt("allowedCollab");
				}
				resultSet.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return allowedNo;
	}

	public int getCurrentCollaborators(Connection con) {
		System.out
				.println("In AllowedCollaboratorServlet::getCurrentCollaborators");
		int currentNo = 0;
		Statement statement = null;
		String sql = null;
		ResultSet resultSet = null;
		try {
			if (con != null) {
				statement = con.createStatement();
				// Result set get the result of the SQL query
				System.out.println("select * from userdetails_" + projectName
						+ " where action='COLLAB_STARTED'");
				sql = "select * from userdetails_" + projectName
						+ " where action='COLLAB_STARTED'";
				resultSet = statement.executeQuery(sql);
				while (resultSet.next()) {
					resultSet.last();
					currentNo = resultSet.getRow();
					System.out.println("Total::+" + currentNo);
				}
				resultSet.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return currentNo;
	}

}
