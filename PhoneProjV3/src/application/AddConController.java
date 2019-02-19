package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddConController extends CVController{
	
	@FXML TextField nameField, numberField;
	@FXML Button accept, cancel;
	
	private SQLiteD sqld = new SQLiteD();
	
	public void addContact(ActionEvent event) { 
		/*
		 * If the accept button has been pressed, the method takes the text from the different text fields 
		 * and creates a new SQL item in the table with the given values.
		 * It then closes the window.
		 */
		if(nameField != null || numberField != null) { 
			String name = nameField.getText();
			String number = numberField.getText();
			sqld.insert(name, number);
			Stage stage = (Stage) accept.getScene().getWindow();
		    stage.close();
		}
	}
	
	public void cancel(ActionEvent event) {
		/*
		 * If the cancel button has been pressed, the window closes without doing anything.
		 */
		Stage stage = (Stage) cancel.getScene().getWindow();
	    stage.close();
	}
}
