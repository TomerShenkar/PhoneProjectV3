package application;

import java.sql.*;
import java.util.ArrayList;

public class SQLiteD {

	/**
	 * This method connects to the SQL database.
	 * <p>
	 * The database is stored in the Data folder of the project.
	 * 
	 * @param None
	 * @return The connection with the database.
	 */
	private Connection connect() {
		String url = "jdbc:sqlite:src/Data/PH";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}

	/**
	 * This method return the entire table.
	 * <p>
	 * The method uses the same connection from the method connect, a statement (the
	 * SQL command) and a ResultSet that stores the results retrieved from the
	 * statement.
	 * 
	 * @param None
	 * @return Everything that's in the table of the database
	 */
	public String[] selectAll() {
		String sql = "SELECT * FROM con";

		try (Connection conn = this.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
			ArrayList<String> names = new ArrayList<String>();
			while (rs.next()) {
				names.add(rs.getString("Name") + ": " + rs.getString("Number") + " ");
			}

			String[] resArray = new String[names.size()];
			resArray = names.toArray(resArray);
			return resArray;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * This method searches a specific name from the table, when given the number.
	 * <p>
	 * Used to detect numbers and assign contacts to them.
	 * <p>
	 * Works the same way as the general search, only this returns one specific
	 * result and doesn't use am array to hold all the results.
	 * 
	 * @param
	 * @return A specific searched result
	 */
	public String searchName(String searchParam) {
		String sql = "SELECT * FROM con Where Number = " + "'" + searchParam + "'";

		try (Connection conn = this.connect();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				return rs.getString("Name");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return searchParam;
	}

	/**
	 * This method inserts new values into the database.
	 * <p>
	 * This method uses prepared statement method, which allows easy insertion of
	 * values into the string.
	 * <p>
	 * The prepared statement then executes the statement with the values inside.
	 * 
	 * @param
	 * @return None
	 */
	public void insert(String insName, String insNumber) {
		String sql = "INSERT INTO con(Name, Number) VALUES(?,?);";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, insName);
			pstmt.setString(2, insNumber);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method updates a specific value of the table. It uses four variables: the first, updating column; Second, updating parameter; 
	 * Third, searching column and fourth, the searching parameter.    
	 * @param 
	 * @return None
	 */
	public void update(String updatingParamColumn, String updatingParam, String searchingParamColumn, String searchingParam) {
		String sql = "UPDATE con SET " + updatingParamColumn + " = ?" + "WHERE " + searchingParamColumn + " = ?";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, updatingParam);
			pstmt.setString(2, searchingParam);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method is used to delete a row from the table.
	 * @param  
	 * @return None
	 */
	public void delete(String columnName, String deleteValue) {
		String sql = "DELETE FROM con WHERE " + columnName + " = ?";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, deleteValue);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
