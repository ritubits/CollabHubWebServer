package org.apache.collab.server;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This servlet is invoked by the COGEclipseClient and gets invoked when the
 * project owner invokes the project registration menu. It registers the project
 * with the MySQL DB server and creates the required tables.
 * 
 * @author Ritu Arora
 * 
 */
public class RegisterProjectServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	String projectName = null;
	String ownerName = null;
	String levelNumber = null;
	String collabNumber = null;
	String ipAddTomcat = null;
	String ipAddSQL = null;
	PrintWriter out = null;
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
		// PrintWriter out = response.getWriter();

		if (DEBUG.contains("TRUE"))
			System.out.println(" In the RegisterProjectServlet ");
		projectName = request.getParameter("pName");
		ownerName = request.getParameter("oName");
		levelNumber = request.getParameter("levelNumber");
		collabNumber = request.getParameter("collabNumber");

		// check if the configProject file exists
		// if yes, append, else create and append
		if (DEBUG.contains("TRUE")) {
			System.out.println("projectName: " + projectName);
			System.out.println("ownerName: " + ownerName);
			System.out.println("ipAddress Tomcat: " + ipAddTomcat);
			System.out.println("ipAddress SQL: " + ipAddSQL);
		}
		// make connection to DB
		// create table regproject_projectName
		// columns: ProjectName, OwnerName and Tomcat_IP
		Connection con = LoadDriver.createConnection(ipAddSQL);
		createTableRegProject(con);
		addDatatoTable(con);
		createTableUserDetails(con);

		// create a table for storing conflict messages, if not already exist
		createTableConflictMessages(con);
		LoadDriver.closeConnection();
	}// doGet

	/**
	 * Inserts data into the project registration table.
	 * 
	 * @param conn
	 */
	public void addDatatoTable(Connection conn) {
		Statement statement = null;

		try {

			if (DEBUG.contains("TRUE"))
				System.out
						.println("inserting data in table in given database...");
			statement = conn.createStatement();

			String sql = "INSERT INTO regProject"
					+ // +projectName+
					" VALUES ('" + projectName + "','" + ownerName + "','"
					+ ipAddTomcat + "','" + levelNumber + "','" + collabNumber
					+ "');";
			if (DEBUG.contains("TRUE"))
				System.out.println("SQL: " + sql);
			statement.executeUpdate(sql);

			if (DEBUG.contains("TRUE"))
				System.out.println("inserted data...");

		} catch (SQLException ex) {
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			out.print("Exception:" + ex.getMessage());
		}

	}

	/**
	 * Creates the project registration table.
	 * 
	 * @param conn
	 */
	public void createTableRegProject(Connection conn) {
		Statement statement = null;

		deleteFromRegProjectTable(conn);

		// create table here
		try {

			if (DEBUG.contains("TRUE"))
				System.out.println("Creating table in given database...");
			statement = conn.createStatement();

			String sql = "CREATE TABLE IF NOT EXISTS regProject"
					+ "(projectName VARCHAR(30) not NULL, "
					+ " ownerName VARCHAR(30), " + " ipAddTomcat VARCHAR(30), "
					+ " levelNumber VARCHAR(10), "
					+ " allowedCollab VARCHAR(2), "
					+ " PRIMARY KEY ( projectname ))";
			if (DEBUG.contains("TRUE"))
				System.out.println("SQL: " + sql);

			statement.executeUpdate(sql);
			if (DEBUG.contains("TRUE"))
				System.out.println("Created table in given database...");

		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			out.print("Exception:" + ex.getMessage());
		}

	}

	/**
	 * Deletes data from the project registration table if it already exists.
	 * 
	 * @param conn
	 */
	public void deleteFromRegProjectTable(Connection conn) {

		Statement statement = null;
		java.sql.DatabaseMetaData meta;
		String sql = null;
		try {
			meta = conn.getMetaData();
			String tableName = null;
			ResultSet res = meta.getTables(null, null, null,
					new String[] { "TABLE" });
			System.out.println("List of tables: ");
			while (res.next()) {

				tableName = res.getString("TABLE_NAME");

				if (tableName.contains("regproject")) {
					// regproject Table exists
					// delete * from it
					statement = conn.createStatement();
					sql = "DELETE FROM " + tableName;

					statement.executeUpdate(sql);

				}// if
			}// while
			res.close();

		} catch (SQLException ex) {

			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());

		}

	}// delete if exists

	/**
	 * Creates user details table in the MySQL DB. This is done at the time of
	 * registring the project with the server.
	 * 
	 * @param conn
	 */
	public void createTableUserDetails(Connection conn) {
		Statement statement = null;

		// create table here
		try {
			deleteFromUserDetailsTable(conn);
			if (DEBUG.contains("TRUE"))
				System.out
						.println("Creating table user details in given database...");
			statement = conn.createStatement();

			String sql = "CREATE TABLE IF NOT EXISTS userdetails_"
					+ projectName + "(projectname VARCHAR(30) not NULL, "
					+ " collabName VARCHAR(30), " + " action VARCHAR(30))";
			if (DEBUG.contains("TRUE"))
				System.out.println("SQL: " + sql);

			statement.executeUpdate(sql);
			if (DEBUG.contains("TRUE"))
				System.out
						.println("Created table user details in given database...");

		} catch (SQLException ex) {

			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			out.print("Exception:" + ex.getMessage());
		}

	}

	/**
	 * Deletes existing details from all the userdetails tables.
	 * 
	 * @param conn
	 */
	public void deleteFromUserDetailsTable(Connection conn) {

		Statement statement = null;
		java.sql.DatabaseMetaData meta;
		String sql = null;
		try {
			meta = conn.getMetaData();
			String tableName = null;
			ResultSet res = meta.getTables(null, null, null,
					new String[] { "TABLE" });
			System.out.println("List of tables: ");
			while (res.next()) {

				tableName = res.getString("TABLE_NAME");

				if (tableName.contains("userdetails_")) {
					statement = conn.createStatement();
					sql = "DELETE FROM " + tableName;

					statement.executeUpdate(sql);

				}// if
			}// while
			res.close();

		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());

		}

	}// delete if exists

	/**
	 * Creates the conflict messages table in the MySQL DB
	 * 
	 * @param conn
	 */
	public void createTableConflictMessages(Connection conn) {
		Statement statement = null;

		// create table here
		try {

			if (DEBUG.contains("TRUE"))
				System.out
						.println("Creating conflictMessages table in given database...");
			statement = conn.createStatement();

			String sql = "CREATE TABLE IF NOT EXISTS conflictMessages"
					+ "(sentNode VARCHAR(30) not NULL, "
					+ " message VARCHAR(200), " + " collabName VARCHAR(30), "
					+ " messagetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP) ";

			if (DEBUG.contains("TRUE"))
				System.out.println("SQL: " + sql);

			statement.executeUpdate(sql);
			if (DEBUG.contains("TRUE"))
				System.out.println("Created table in given database...");

		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			out.print("Exception:" + ex.getMessage());
		}

	}
}
