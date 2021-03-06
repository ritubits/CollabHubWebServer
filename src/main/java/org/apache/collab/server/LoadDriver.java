package org.apache.collab.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This class is responsible for creating a connection with the MySQL DB.
 * 
 * @author Ritu Arora
 * 
 */
public class LoadDriver {

	public static Connection connect = null;

	public static Connection getConnection() {
		return connect;
	}

	public static Connection createConnection(String ipAddSQL) {
		try {

			Class.forName("com.mysql.jdbc.Driver").newInstance();
			System.out.println("LoadingDriver Suceeded");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			System.out.println("ipAddSQL: " + ipAddSQL);
			// DriverManager.getConnection("jdbc:mysql://localhost:3306/collaborationhub?"+"user=root&password=ritu");
			connect = DriverManager.getConnection("jdbc:mysql://" + ipAddSQL
					+ "/collaborationhub?" + "user=root&password=ritu");
			System.out.println("Connection Suceeded");

		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		return connect;
	}

	public static void closeConnection() {
		try {
			connect.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}