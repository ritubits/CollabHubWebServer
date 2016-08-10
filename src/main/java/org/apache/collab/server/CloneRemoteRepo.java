package org.apache.collab.server;

/* IMPORTANT
 * This code clones the repository from github
 * works fine

 * clones at d:\apache..\temp
 * */

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;

public class CloneRemoteRepo implements Runnable {

	//private static final String REMOTE_URL = "https://github.com/ritubits/CQPAss1.git";
	private static final String REMOTE_URL = "https://github.com/ritubits/MathTutorialProject.git";
	private static final String PATH_URL = "D:\\TestGitProjectRepo";
	private static final String SRC_URL = "D:\\TestGitProjectRepo\\MathProject\\src\\twoDShapes";


	private String ipAddSQL;
	private String projectName;
	Connection con;

	public CloneRemoteRepo(String pName, String ipAddDB) {
		ipAddSQL = ipAddDB;
		projectName = pName;

	try {
			con = LoadDriver.createConnection(ipAddSQL);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		// TODO Auto-generated method stub

		File filePath = null;
		try {
			System.out.println("in clone::");
			for (;;) {

				if (filePath != null && filePath.exists())
					removeDirectory(filePath);

				filePath = File.createTempFile("TestGitRepository", "123");
				filePath.delete();
				System.out.println("filePath: " + filePath.toString());

				// then clone
				System.out.println("Cloning from " + REMOTE_URL + " to "
						+ filePath);
				Git result = Git.cloneRepository().setURI(REMOTE_URL)
						.setDirectory(filePath).call();

				System.out.println("Having repository: "
						+ result.getRepository().getDirectory());

				result.getRepository().close();

				copyToProjectRepo(filePath, con);

				CreateDependencyGraph dbGraph= new CreateDependencyGraph();
			    try {
			    	System.out.println("Going to create DB");
					dbGraph.initializeDB(projectName, SRC_URL);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// delet contents of the directory filePath
				removeDirectory(filePath);

				// change this to 20 minutes
				Thread.sleep(60 * 3000);// 3min

			}// for
		}// try
		catch (Exception e) {
			e.printStackTrace();
		}

	}// run

	public void copyToProjectRepo(File source, Connection conn) {
		File dest = new File(PATH_URL);

		// delete the content if they exist
		// if (dest!= null && dest.exists()) removeDirectory(dest);

		try {
			System.out.println("Copying files from: " + source.toString()
					+ "to: " + dest.toString());

			FileUtils.copyDirectory(source, dest);

			// create graph and delete the folder
			// create table here
			createArtifactTable(conn);
			System.out.println("Going yo create DB");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void removeDirectory(File dir) {
		// remove all files and folders in the specified file path and the
		// directory also
		// System.out.println("Deleting files from: "+ dir.toString());
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			if (files != null && files.length > 0) {
				for (File aFile : files) {
					removeDirectory(aFile);
				}
			}
			dir.delete();
		} else {
			dir.delete();
		}
	}

	public void createArtifactTable(Connection conn) {

		Statement statement = null;
		java.sql.DatabaseMetaData meta;
		String sql=null;
		try {
			meta = conn.getMetaData();
			String tableName = null;
			ResultSet res = meta.getTables(null, null, null,
					new String[] { "TABLE" });
			System.out.println("List of tables: ");
			while (res.next()) {

				tableName = res.getString("TABLE_NAME");

				if (tableName.contains("artifact_")) {
					// artifact Table exists
					// delete * from it
					statement = con.createStatement();
					sql = "DELETE FROM " + tableName;

					statement.executeUpdate(sql);

				}// if
			}// while
			res.close();

			System.out.println("Creating table artifact_projectName");
			statement = conn.createStatement();

			sql = "CREATE TABLE IF NOT EXISTS artifact_" + projectName
					+ "(projectName VARCHAR(30), filename VARCHAR(30))";

			System.out.println("SQL: " + sql);
			statement.executeUpdate(sql);

		} catch (SQLException ex) {
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());

		}

		// enter data into the table
		// need to parse the entire structure from PATH_URL to obtain the names
		// of java files
		enterDataInArtifactTable(conn);

	}// create

	public void enterDataInArtifactTable(Connection conn) {

		/*
		 * File dirs = new File(PATH_URL); String dirPath= null;
		 * 
		 * try { dirPath = dirs.getCanonicalPath() +
		 * File.separator+"src"+File.separator+"gitHubConnectGraph\\"; } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

		// enter data into artifact_projectname
		File root = new File(SRC_URL);
		File[] files = root.listFiles();
		File f;
		for (int i = 0; i < files.length; i++) {

			f = files[i];
			if (f.isFile()) {
				// print filename
				System.out.println("In file: " + f.getName());
				if (f.getName().contains(".java")) {
					// insert into table
					Statement statement = null;

					try {

						System.out
								.println("inserting data in artifact table in given database...");
						statement = conn.createStatement();

						String sql = "INSERT INTO artifact_" + projectName
								+ " VALUES ('" + projectName + "','"
								+ f.getName() + "')";
						System.out.println("SQL: " + sql);
						statement.executeUpdate(sql);

						System.out.println("inserted data...");

					} catch (SQLException ex) {
						// handle any errors
						System.out.println("SQLException: " + ex.getMessage());
						System.out.println("SQLState: " + ex.getSQLState());
						System.out.println("VendorError: " + ex.getErrorCode());
					}
				}
			}
		}
	}

	public void destroy() {
		try {
			// drop table if exists
			// DROP TABLE IF EXISTS AgentDetail

			if (con != null) {
				Statement statement = con.createStatement();

				System.out.println("drop table table artifact_...");
				statement = con.createStatement();

				String sql = "DROP TABLE IF EXISTS artifact_" + projectName;

				System.out.println("SQL: " + sql);

				statement.executeUpdate(sql);

				con.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
