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
 * collaborator invokes the stop collaboration menu. It create the required the
 * tables (user_activity table) with the MySQL DB server.
 * 
 * @author Ritu Arora
 * 
 */
public class StopCollaborationServlet extends HttpServlet {

	// this servlet will connect to DB and
	// DROP TABLE USERACTIVITY- LATER
	// remove from userdetails table the row with the collabname

	private static final long serialVersionUID = 1L;
	String projectName = null;
	String collabName = null;
	String ipAddTomcat = null;
	String ipAddSQL = null;
	PrintWriter out = null;
	static Connection con = null;
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
			System.out.println(" In the StopProjectServlet ");
		projectName = request.getParameter("pName");
		collabName = request.getParameter("cName");

		if (DEBUG.contains("TRUE")) {
			System.out.println("projectName: " + projectName);
			System.out.println("collabName: " + collabName);
			System.out.println("ipAddress Tomcat: " + ipAddTomcat);
			System.out.println("ipAddress SQL: " + ipAddSQL);
		}

		con = LoadDriver.connect;
		if (con != null)

		{
			RemoveDatafromTable(con);
			RemoveTableUserActivity(con);
		} else {
			System.out.println("No connection exists:");// need to forward this
														// error
		}
		// do not close this connection

	}// doGet

	/**
	 * This method deletes the data from the user details data since the
	 * collaborator stops collaboration.
	 * 
	 * @param conn
	 */
	public void RemoveDatafromTable(Connection conn) {
		Statement statement = null;

		// insert data here
		try {

			if (DEBUG.contains("TRUE"))
				System.out
						.println("removing data from userDetails table in given database...");
			statement = conn.createStatement();

			statement = conn.createStatement();
			String sql = "DELETE FROM userdetails_" + projectName
					+ " WHERE collabname = '" + collabName + "'";
			if (DEBUG.contains("TRUE"))
				System.out.println(sql);
			statement.executeUpdate(sql);

			if (DEBUG.contains("TRUE"))
				System.out
						.println("removed data from userDetails table in given database...");

		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			out.print("Exception:" + ex.getMessage());
		}

	}

	/**
	 * Drops the corresponding user activity table for the collaborator who has
	 * stopped collaboration.
	 * 
	 * @param conn
	 */
	public void RemoveTableUserActivity(Connection conn) {
		Statement statement = null;

		// insert data here
		try {

			if (DEBUG.contains("TRUE"))
				System.out
						.println("removing data from activity table in given database...");
			statement = conn.createStatement();

			statement = conn.createStatement();
			String sql = "DROP TABLE IF EXISTS useractivity_" + collabName;
			if (DEBUG.contains("TRUE"))
				System.out.println(sql);
			statement.executeUpdate(sql);

			if (DEBUG.contains("TRUE"))
				System.out
						.println("removed data from activity table in given database...");

		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			out.print("Exception:" + ex.getMessage());
		}

	}
}
