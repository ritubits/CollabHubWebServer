package org.apache.collab.server;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This servlet is an important part of the UserActivityAnalyzer Component of
 * the COG server. It is invoked by the COGEclipseClient and gets invoked at
 * regular intervals. Once the collaboration is started, the user sends its
 * activity metadata at regular intervals, by invoking this servlet.
 * 
 * @author Ritu Arora
 * 
 */
public class UserActivityServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	String ipAddSQL = null;
	String currentFile = null;
	String currentAST = null;
	String currentLine = null;
	String editFiles = null;
	String[] editFilesArray = null;
	String collabName = null;

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
		ipAddSQL = getServletContext().getInitParameter("ipAddSQL");
		DEBUG = getServletContext().getInitParameter("DEBUG");
	}

	/**
	 * Obtains data from incoming HttpObject and updates the user activity table
	 * in the MySQl DB.
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		response.setContentType("text/html");

		System.out.println(" In the UserActivityServlet ");
		currentFile = request.getParameter("cFile");
		currentLine = request.getParameter("cLine");
		currentAST = request.getParameter("cAST");
		editFiles = request.getParameter("eFile");
		System.out.println("Edit Files from:: SErvlet:: " + editFiles);
		if (!editFiles.contains("null"))
			editFilesArray = parseToStringArray(editFiles);

		collabName = request.getParameter("cName");

		if (DEBUG.contains("TRUE"))
			System.out.println("ipAddress SQL: " + ipAddSQL);

		// make connection to DB

		Connection con = LoadDriver.connect;
		if (con != null) {
			updateUserActivityTable(con);
		} else {
			System.out.println("No connection exists:");// need to forward this
														// error
		}
		// do not close this connection

	}// doGet

	/**
	 * Updates the user activity table with the activity metadata information
	 * received from the collaborator.
	 * 
	 * @param conn
	 */
	public void updateUserActivityTable(Connection conn) {
		Statement statement = null;
		String sql = null;

		// insert data here
		try {

			if (DEBUG.contains("TRUE"))
				System.out
						.println("inserting data in userActivityDetails table in given database...");
			statement = conn.createStatement();

			sql = "INSERT INTO useractivity_"
					+ collabName
					+ " (filename, elementName, lineNo, activitytype) VALUES  ('"
					+ currentFile + "','" + currentAST + "','" + currentLine
					+ "','EDIT');";
			if (DEBUG.contains("TRUE"))
				System.out.println("SQL: " + sql);
			statement.executeUpdate(sql);

			if (!editFiles.contains("null")) {
				for (int i = 0; i < editFilesArray.length; i++) {

					sql = "INSERT INTO useractivity_"
							+ collabName
							+ " (filename, elementName, lineNo, activitytype) VALUES  ('"
							+ editFilesArray[i] + "','NULL','0','OPEN');";
					if (DEBUG.contains("TRUE"))
						System.out.println("SQL: " + sql);
					statement.executeUpdate(sql);
				}
			}
			if (DEBUG.contains("TRUE"))
				System.out.println("inserted data in userActivityDetails...");

		} catch (SQLException ex) {
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			out.print("Exception:" + ex.getMessage());
		}

	}

	public String[] parseToStringArray(String eFiles) {
		String[] temp1;
		String delimiter1 = "[,]";
		temp1 = eFiles.split(delimiter1);
		if (DEBUG.contains("TRUE"))
			System.out.println("From strat: " + temp1);

		return temp1;
	}

}
