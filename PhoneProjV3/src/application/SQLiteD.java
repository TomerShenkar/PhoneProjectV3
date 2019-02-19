package application;

import java.sql.*;
import java.util.ArrayList;

public class SQLiteD {
	private Connection connect() {
		/*
		 * This method connects to the SQL database.
		 * The database is stored in the Data folder in the project. 
		 */
		String url = "jdbc:sqlite:src/Data/PH";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
          e.printStackTrace();
        }
        return conn;	
	}
	
    public String[] selectAll(){ 
    	/*
    	 * This method return the entire table.
    	 * The method uses the same connection from the method connect, a statement (the SQL command)
    	 * and a ResultSet that stores the results retrieved from the statement. 
    	 */
        String sql = "SELECT * FROM con";
        
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)){
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
	
    public String searchName(String searchParam){
    	/*
    	 * This method searches a specific name from the table, when given the number. 
    	 * Used to detect numbers and assign contacts to them.
    	 * Works the same way as the general search, 
    	 * only this returns one specific result and doesn't use am array to hold all the results.
    	 */
        String sql = "SELECT * FROM con Where Number = " + "'" + searchParam + "'";
        
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)){
            while (rs.next()) {
            	return rs.getString("Name");
            }
            
        } catch (SQLException e) {
        	e.printStackTrace();
          }
        
        return searchParam;
      }
    
    public void insert(String insName, String insNumber) {
    	/*
    	 * This method inserts new values into the database.
    	 * This method used the prepared statement method, which allows easy 
    	 * insertion of values into the string. 
    	 * The prepared statement then executes the statement with the values inside. 
    	 */
        String sql = "INSERT INTO con(Name, Number) VALUES(?,?);";
 
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, insName);
            pstmt.setString(2, insNumber);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public void update(String updatingParamColumn, String updatingParam, String searchingParamColumn, String searchingParam) {
        String sql = "UPDATE con SET " + updatingParamColumn + " = ?" + "WHERE " + searchingParamColumn + " = ?";
 
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
 
            // set the corresponding param
            pstmt.setString(1, updatingParam);
            pstmt.setString(2, searchingParam);
            // update s
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public void delete(String columnName, String deleteValue) {
    	   	String sql = "DELETE FROM con WHERE " + columnName + " = ?";
    	   	
    	 	try(Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)){
    		//set the corresponding param
    		pstmt.setString(1, deleteValue);
    		//update
    		pstmt.executeUpdate();
    	}
    	catch(SQLException e) {
    		System.out.println(e);
    	}
    }
}
