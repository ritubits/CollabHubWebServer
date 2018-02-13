package org.apache.collab.server;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;

/**
 * This thread belongs to the GitHubCollaborator Component of the
 * UserCollaborationEngine of COG. It is the main class responsible for cloning
 * remote project repository from GitHub. The InitiateCollabHub HttpServlet
 * creates an instance of this class and invokes the run() method.
 * 
 * @author Ritu Arora
 * @version 1.1
 * 
 */
public class CloneRemoteRepo implements Runnable {

	// private static final String REMOTE_URL =
	// "https://github.com/ritubits/CQPAss1.git";
	// private static final String REMOTE_URL =
	// "https://github.com/ritubits/MathTutorialProject.git";
	// private static final String PATH_URL = "D:\\TestGitProjectRepo";
	// private static final String SRC_URL =
	// "D:\\TestGitProjectRepo\\MathProject\\src\\twoDShapes";

	private String REMOTE_URL = null;
	private String PATH_URL = null;
	private String SRC_URL = null;

	private String ipAddSQL;
	private String projectName;
	Connection con;

	/**
	 * 
	 * @param pName
	 *            Name of the Java Project to be cloned
	 * @param ipAddDB
	 *            IPAddress of the MySQL database
	 * @param rPath
	 *            URL of the remote GitHub repository
	 * @param tPath
	 *            Path where the repository is to be cloned
	 * @param sPath
	 *            Path of the src folder for cloning the repository
	 */
	public CloneRemoteRepo(String pName, String ipAddDB, String rPath,
			String tPath, String sPath) {
		ipAddSQL = ipAddDB;
		REMOTE_URL = rPath;
		PATH_URL = tPath;
		SRC_URL = sPath;
		projectName = pName;

		try {
			con = LoadDriver.createConnection(ipAddSQL);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method clones the remote GitHub repository from Remote_URL to a
	 * temporary location and then copies the same to PATH_URL and deletes from
	 * temporary location. It then creates an object of CreateDependencyGraph
	 * and calls the initializeDB() method for creating the dependency graph of
	 * the cloned repository using Neo4j Graph DB.
	 */
	public void run() {
		File filePath = null;
		try {
			System.out.println("in clone::");
			for (;;) {

				if (filePath != null && filePath.exists())
					removeDirectory(filePath);

				filePath = File.createTempFile("TestGitRepository", "123");
				filePath.delete();
				System.out.println("filePath: " + filePath.toString());

				// cloning
				System.out.println("Cloning from " + REMOTE_URL + " to "
						+ filePath);
				Git result = Git.cloneRepository().setURI(REMOTE_URL)
						.setDirectory(filePath).call();

				System.out.println("Having repository: "
						+ result.getRepository().getDirectory());

				result.getRepository().close();

				copyToProjectRepo(filePath, con);

				CreateDependencyGraph dbGraph = new CreateDependencyGraph();
				try {
					System.out.println("Going to create DB");
					dbGraph.initializeDB(projectName, SRC_URL);

				} catch (Exception e) {
					e.printStackTrace();
				}

				// delete contents of the directory filePath
				removeDirectory(filePath);

				// this is 2 hours (1s= 1000ms)
				Thread.sleep(1000 * 60 * 60 * 2);

			}// for

		}// try
		catch (Exception e) {
			e.printStackTrace();
		}

	}// run

	/**
	 * Copies repository files to the specified location.
	 * 
	 * @param source
	 * @param conn
	 */
	public void copyToProjectRepo(File source, Connection conn) {
		File dest = new File(PATH_URL);

		try {
			System.out.println("Copying files from: " + source.toString()
					+ "to: " + dest.toString());

			FileUtils.copyDirectory(source, dest);

			// create graph and delete the folder
			// create table here
			createArtifactTable(conn);
			System.out.println("Going to create DB");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param dir
	 */
	public void removeDirectory(File dir) {
		// remove all files and folders in the specified file path and the
		// directory also
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

	/**
	 * This method creates the tabke named artifact_projectName in MySQL DB and
	 * fills it with the names of all Java files in the project.
	 * 
	 * @param conn
	 *            Connection to the MySQL DB
	 */
	public void createArtifactTable(Connection conn) {

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

			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());

		}

		// enter data into the table
		// need to parse the entire structure from PATH_URL to obtain the names
		// of java files
		enterDataInArtifactTable(conn);

	}// create

	/**
	 * This method parses the directory structure of the cloned repository to
	 * obtain the names of all Java files. These are entered into the
	 * artifact_projectName table in the MySQL DB.
	 * 
	 * @param conn
	 *            Connection to the MySQL DB
	 */
	public void enterDataInArtifactTable(Connection conn) {

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

	/**
	 * Drops the associated tables from MySQL DB, it they exist
	 */
	public void destroy() {
		try {
			// drop table if exists
			// DROP TABLE IF EXISTS

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

			e.printStackTrace();
		}
	}

}
