package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditConController extends CVController{
	
	@FXML TextField nameField, numberField;
	@FXML Button accept, cancel;
	
	private SQLiteD sqld = new SQLiteD();
	
	public void editCon(ActionEvent event) {
		/*
		 * This method works in the following way, only if the accept key is pressed:
		 * First, the searchName needed for the SQL edit is retrieved.
		 * Then, the values from the textFields are retrieved.
		 * If there's only a name, only that name will be updated, and same goes for number.
		 * If both are changes, both values will be updated.
		 * After the update occurs, the window is closed.
		 */
		String searchName = selectedName;
		if(nameField != null || numberField != null || searchName == "" || searchName == null) {
			String newName = nameField.getText();
			String newNumber = numberField.getText();
			
			if(newName.equals("") || newName == null){
				sqld.update("Number", newNumber, "Name", searchName);
				Stage stage = (Stage) cancel.getScene().getWindow();
			    stage.close();
			}
			else if(newNumber.equals("") || newNumber == null){
				sqld.update("Name", newName, "Name", searchName);
				Stage stage = (Stage) cancel.getScene().getWindow();
			    stage.close();
			}
			else
				sqld.update("Name", newName, "Name", searchName);
				sqld.update("Number", newNumber, "Name", searchName);
			Stage stage = (Stage) cancel.getScene().getWindow();
		    stage.close();
		}
	}
	
	public void cancel(ActionEvent event) {
		/*
		 * If the cancel key is pressed, the window closes with no action happening.
		 */
		Stage stage = (Stage) cancel.getScene().getWindow();
	    stage.close();
	}
}
