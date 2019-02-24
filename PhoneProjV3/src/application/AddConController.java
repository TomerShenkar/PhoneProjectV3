package application;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddConController extends CVController{
	@FXML TextField nameField;
	@FXML TextField numberField;
	@FXML Button accept;
	@FXML Button cancel;
	private SQLiteD sqld = new SQLiteD();
	@ Override
	/**
	 * This method is first called when this window starts.
	 * Used to load the edited names into the textFields. 
	 */
	public void initialize(URL arg0, ResourceBundle arg1) {
		nameField.setPromptText("Enter Name");
		numberField.setPromptText("Enter Number");
	}
	
	
	/**
	 * If the accept button has been pressed, the method takes the text from the different text fields 
	 * and creates a new SQL item in the table with the given values.
	 * It then closes the window.
	 * @param event
	 * @return None
	 */
	public void addContact(ActionEvent event) { 
		if(nameField != null || numberField != null) { 
			String name = nameField.getText();
			String number = numberField.getText();
			sqld.insert(name, number);
			Stage stage = (Stage) accept.getScene().getWindow();
		    stage.close();
		}
	}
	/**
	 * If the cancel button has been pressed, the window closes without doing anything.
	 * @param event
	 * @return None
	 */
	public void cancel(ActionEvent event) {
		Stage stage = (Stage) cancel.getScene().getWindow();
	    stage.close();
	}
}
