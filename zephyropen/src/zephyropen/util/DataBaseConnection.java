package zephyropen.util;

import java.sql.*;

public class DataBaseConnection {

	Connection conn;

	public static void main(String[] args) {
		new DataBaseConnection();
	}

	public DataBaseConnection() {
		try {
			
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			String url = "jdbc:mysql://localhost/brad";
			conn = DriverManager.getConnection(url, "brent", "zdy");

			doInsertTest();
			doSelectTest();

			conn.close();
			
		} catch (ClassNotFoundException ex) {
			System.err.println(ex.getMessage());
		} catch (IllegalAccessException ex) {
			System.err.println(ex.getMessage());
		} catch (InstantiationException ex) {
			System.err.println(ex.getMessage());
		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}
	}


	private void doSelectTest() {
		System.out.println("[OUTPUT FROM SELECT]");
		String query = "SELECT * FROM POLAR";
		try {
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(query);
			int i = 0;
			while (rs.next()) {
				String s = rs.getString("timestamp");
				float n = rs.getFloat("heart");
				System.out.println(i++ + " " + s + " = " + n);
			}
		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}
	}

	private void doInsertTest() {

		for (int i = 0; i < 100 ; i++) {

			if( i % 100 == 0 ) System.out.println(i);

			try {
				Statement st = conn.createStatement();

				st.executeUpdate("INSERT INTO POLAR " + "VALUES (" + i + ", " + i + ")");

			} catch (SQLException ex) {
				System.err.println(ex.getMessage());
			}
		}

	}

	private void doDeleteTest() {
		System.out.print("\n[Performing DELETE] ... ");
		try {
			Statement st = conn.createStatement();
			st.executeUpdate("DELETE FROM COFFEES WHERE COF_NAME='BREAKFAST BLEND'");
		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}
	}
}
