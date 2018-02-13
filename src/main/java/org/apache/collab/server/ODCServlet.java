package org.apache.collab.server;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Vector;

/**
 * This is the servlet that provides reads, filters and passes the activity
 * metadata to the Open-Direct-Collaborators View of the
 * collaboratorsInfoViewParts plugin of the COGEclipseClient.
 * 
 * @author Ritu Arora
 * 
 */
public class ODCServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	String ipAddSQL = null;
	Connection con = null;
	String DEBUG = null;

	public void init(ServletConfig config) throws ServletException {

		// read parameters from web.xml
		// read ipAddTomcat and ipAddSQL
		super.init(config);
		ipAddSQL = getServletContext().getInitParameter("ipAddSQL");
		DEBUG = getServletContext().getInitParameter("DEBUG");
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String collabName = null;

		Vector<String> activityTables = new Vector<String>();

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		System.out.println(" In the ODCServlet ");
		collabName = request.getParameter("cName");

		if (DEBUG.contains("TRUE"))
			System.out.println("ipAddress SQL: " + ipAddSQL);

		// make connection to DB

		// con= LoadDriver.createConnection(ipAddSQL);//connect;
		con = LoadDriver.connect;
		String artifact = null;
		String collabNames = null;
		if (con != null) {
			artifact = getEditArtifact(collabName);
			collabNames = getODC(artifact, activityTables);
			// messages=getConflictMessages(con);
			System.out.println("EditArtifact:: " + artifact);
			System.out.println("CollabNames:: "
					+ parseCollabNames(collabNames, collabName));
			out.print(parseCollabNames(collabNames, collabName));
		} else {
			// out.print("No Conflict Messages");
			System.out.println("No connection exists:");// need to forward this
														// error
		}
		// do not close this connection

	}// doGet

	public String getEditArtifact(String collabName) {
		String editArtifact = null;
		Statement statement = null;
		String sql = null;
		ResultSet resultSet = null;
		try {
			if (con != null) {

				statement = con.createStatement();
				// Result set get the result of the SQL query
				sql = "select filename, activitytime from useractivity_"
						+ collabName
						+ " where activitytype='EDIT' and activitytime = (select max(activitytime) from useractivity_"
						+ collabName + ");";
				resultSet = statement.executeQuery(sql);
				while (resultSet.next()) {

					editArtifact = resultSet.getString("filename");
					// assumes only one row
				}
				resultSet.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return editArtifact;

	}

	public String getODC(String artifact, Vector activityTables) {
		String collabNames = null;

		try {

			java.sql.DatabaseMetaData meta;

			meta = con.getMetaData();
			String tableName = null;
			Statement statement = null;
			ResultSet res = meta.getTables(null, null, null,
					new String[] { "TABLE" });
			if (DEBUG.contains("TRUE"))
				System.out.println("List of tables: ");
			while (res.next()) {

				tableName = res.getString("TABLE_NAME");
				if (tableName.contains("useractivity")) {
					// add to table array for activities
					activityTables.addElement(tableName);
					if (DEBUG.contains("TRUE"))
						System.out
								.println("UserActivity tables:: " + tableName);
				}
			}
			res.close();

			// get data from individual activity table
			statement = con.createStatement();
			String sql = null;
			Enumeration userActivityTableName = activityTables.elements();
			while (userActivityTableName.hasMoreElements()) {
				String usertableName = userActivityTableName.nextElement()
						.toString();
				sql = "select filename from "
						+ usertableName
						+ " where filename='"
						+ artifact
						+ "' and activitytype='OPEN' and MINUTE(activitytime) >= MINUTE(NOW()-INTERVAL 5 MINUTE);";

				ResultSet resultSet = statement.executeQuery(sql);
				int total = 0;
				while (resultSet.next()) {
					resultSet.last();
					total = resultSet.getRow();
					System.out.println("Total::+" + total);
					// assumes only one row
				}
				if (total > 0) {
					// add this to coolab
					if (collabNames == null)
						collabNames = usertableName;
					else
						collabNames = collabNames + "," + usertableName;
				}
				resultSet.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return collabNames;
	}

	public String parseCollabNames(String collabNames, String collabName) {
		String collab = null;
		String s = null;
		if (collabNames != null) {
			String[] temp1;
			String delimiter1 = "[,]";
			temp1 = collabNames.split(delimiter1);
			for (int i = 0; i < temp1.length; i++) {
				System.out.println("i=" + i + temp1[i]);
				int index = temp1[i].indexOf("_");
				s = temp1[i].substring(index + 1, temp1[i].length());

				if (!collabName.equalsIgnoreCase(collab)) {
					if (collab == null)
						collab = s;
					else
						collab = collab + "," + s;
				}
			}

		}
		return collab;
	}
}
