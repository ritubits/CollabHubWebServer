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
 * metadata to the Edit-Direct-Collaborators View of the
 * collaboratorsInfoViewParts plugin of the COGEclipseClient.
 * 
 * @author Ritu Arora
 * 
 */
public class EDCServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	String ipAddSQL = null;
	Connection con = null;
	String DEBUG = "TRUE";

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

		if (DEBUG.contains("TRUE"))
			System.out.println(" In the EDCServlet ");
		collabName = request.getParameter("cName");

		con = LoadDriver.connect;
		String artifact = null;
		String returnData = null;
		if (con != null) {
			artifact = getEditArtifact(collabName);
			if (DEBUG.contains("TRUE"))
				System.out.println("EditArtifact:: " + artifact + "of "
						+ collabName);

			returnData = getEDC(artifact, activityTables, collabName);
			// messages=getConflictMessages(con);

			// System.out.println("CollabNames:: "+parseCollabNames(collabNames,
			// collabName));
			out.print(returnData);
		} else {
			// out.print("No Conflict Messages");
			System.out.println("No connection exists:");// need to forward this
														// error
		}
		// do not close this connection

		// System.out.println("Forwarding to LivePreview JSp");//need to forward
		// this error
		// request.getRequestDispatcher("/LivePreview.jsp").forward(request,response);
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

	public String getEDC(String artifact, Vector activityTables, String cName) {
		String EDCCollabData = null;

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
			String clientName = null;
			Enumeration userActivityTableName = activityTables.elements();
			while (userActivityTableName.hasMoreElements()) {
				String usertableName = userActivityTableName.nextElement()
						.toString();
				// sql= "select filename, elementName, lineNo from "+
				// usertableName+" where filename='"+artifact+"' and activitytype='EDIT' and MINUTE(activitytime) >= MINUTE(NOW()-INTERVAL 5 MINUTE);";
				sql = "select filename,elementName,lineNo from "
						+ usertableName
						+ " where filename='"
						+ artifact
						+ "' and activitytype='EDIT' and activitytime = (select max(activitytime) from "
						+ usertableName + ");";
				if (DEBUG.contains("TRUE"))
					System.out.println(sql);
				ResultSet resultSet = statement.executeQuery(sql);

				while (resultSet.next()) {
					clientName = parse(usertableName);
					if (DEBUG.contains("TRUE"))
						System.out.println("clientName::" + clientName);

					if (!clientName.equalsIgnoreCase(cName)) {
						if (EDCCollabData == null)
							EDCCollabData = clientName + ","
									+ resultSet.getString("elementName") + ","
									+ resultSet.getInt("lineNo");
						else
							EDCCollabData = EDCCollabData + "|" + clientName
									+ "," + resultSet.getString("elementName")
									+ "," + resultSet.getInt("lineNo");
					}
				}
				resultSet.close();
				if (DEBUG.contains("TRUE"))
					System.out.println("EDCCollabData::" + EDCCollabData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return EDCCollabData;
	}

	public String parse(String tableName) {

		int index = tableName.indexOf("_");
		String s = tableName.substring(index + 1, tableName.length());

		return s;
	}
}
