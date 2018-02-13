package org.apache.collab.server;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This servlet is invoked by the COGEclipseClient and gets invoked when the
 * collaborator invokes the starts collaboration menu. It create the required
 * the tables (user_activity table) with the MySQL DB server.
 * 
 * @author Ritu Arora
 * 
 */
public class StartCollaborationServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	String projectName = null;
	String collabName = null;
	String ipAddTomcat = null;
	String ipAddSQL = null;
	PrintWriter out = null;
	Connection con = null;
	String DEBUG = null;

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
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		response.setContentType("text/html");

		if (DEBUG.contains("TRUE"))
			System.out.println(" In the StartCollaborationServlet ");
		projectName = request.getParameter("pName");
		collabName = request.getParameter("cName");

		// check if the configProject file exists
		// if yes, append, else create and append
		if (DEBUG.contains("TRUE")) {
			System.out.println("projectName: " + projectName);
			System.out.println("collabName: " + collabName);
			System.out.println("ipAddress Tomcat: " + ipAddTomcat);
			System.out.println("ipAddress SQL: " + ipAddSQL);
		}
		// make connection to DB

		con = LoadDriver.createConnection(ipAddSQL);
		addDatatoTable(con);

		// do not close this connection
		// use further---- pass to others
		// create table useractivity_collaborator
		createTableUserActivity(con);

	}// doGet

	/**
	 * Adds collaboration details to the user details table in MySQL DB.
	 * 
	 * @param conn
	 * 
	 */
	public void addDatatoTable(Connection conn) {
		Statement statement = null;

		// insert data here
		try {

			if (DEBUG.contains("TRUE"))
				System.out
						.println("inserting data in userDetails table in given database...");
			statement = conn.createStatement();

			String sql = "INSERT INTO userdetails_" + projectName
					+ " VALUES ('" + projectName + "','" + collabName
					+ "','COLLAB_STARTED');";
			if (DEBUG.contains("TRUE"))
				System.out.println("SQL: " + sql);
			statement.executeUpdate(sql);

			if (DEBUG.contains("TRUE"))
				System.out.println("inserted data in userDetails...");

		} catch (SQLException ex) {
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			out.print("Exception:" + ex.getMessage());
		}

	}

	/**
	 * Creates the user activity table with the MySQL DB.
	 * 
	 * @param conn
	 */
	public void createTableUserActivity(Connection conn) {
		Statement statement = null;

		// create table here
		try {

			if (DEBUG.contains("TRUE"))
				System.out.println("Creating table in given database...");
			statement = conn.createStatement();

			String sql = "CREATE TABLE IF NOT EXISTS useractivity_"
					+ collabName + "(filename VARCHAR(30) not NULL, "
					+ " elementName VARCHAR(30), "
					+ " lineNo INTEGER not NULL, "
					+ " activitytime TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
					+ " activitytype VARCHAR(30))";

			if (DEBUG.contains("TRUE"))
				System.out.println("SQL: " + sql);

			statement.executeUpdate(sql);
			if (DEBUG.contains("TRUE"))
				System.out
						.println("Created table USERaCTIVITY in given database...");

		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			out.print("Exception:" + ex.getMessage());
		}

	}

	public void destroy() {
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
