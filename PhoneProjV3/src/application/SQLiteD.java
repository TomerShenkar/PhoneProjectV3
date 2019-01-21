package application;

import java.sql.*;
import java.util.ArrayList;

public class SQLiteD {
	private Connection connect() {
		String url = "jdbc:sqlite:src/Data/PH";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
            //System.out.println("Successful connection");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;	
	}
	
    public String[] selectAll(){ 
        String sql = "SELECT * FROM con";
        
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)){
            // loop through the result set
        	ArrayList<String> names = new ArrayList<String>();

        	while (rs.next()) {
        	    names.add(rs.getString("Name") + "@" + rs.getString("Number") + " ");
        	}

        	// finally turn the array lists into arrays - if really needed
        	String[] resArray = new String[names.size()];
        	resArray = names.toArray(resArray);
        	
        	return resArray;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;

    }
	
    public void getNameOrNumber(String searchParam){ //searchParam being Name/Number
        String sql = "SELECT " + searchParam + " FROM con";
        
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)){
            
            // loop through the result set
            while (rs.next()) {
                System.out.println(rs.getString(searchParam));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
	
    public String searchName(String searchParam){ //This method searches the name attached to a number given in the main controller 
        String sql = "SELECT * FROM con Where Number Like " + "'" + searchParam + "%'";
        //String sql = "SELECT * FROM con Where " + searchColumn + " = " +  searchParam;
        
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)){
            
            // loop through the result set
            while (rs.next()) {
            	return rs.getString("Name");
            }
            
        } catch (SQLException e) {
              System.out.println(e.getMessage());
          }
        return null;
      }
    
    public String searchSpecific(String searchColumn1, String searchColumn2, String searchParam){ //searchColumn1/2 being Name/Number, searchParam being the search keyword	
      String sql = "SELECT " + searchColumn1 +" FROM con WHERE " + searchColumn2 + " Like " + "'" + searchParam + "%'";
      //SELECT Number FROM con WHERE Name = 'Tomer'; ---> 0546951919
      try (Connection conn = this.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)){
          
          // loop through the result set
          while (rs.next()) {
              return(rs.getString(searchColumn1));
          }
      } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
      return null;
    }
    
    public void insert(String insName, String insNumber) {
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
