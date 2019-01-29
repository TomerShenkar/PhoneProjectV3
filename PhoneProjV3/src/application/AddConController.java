package application;

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
	
	public void addContact(ActionEvent event) {
		if(nameField != null || numberField !=null) {
			String name = nameField.getText();
			String number = numberField.getText();
			sqld.insert(name, number);
			reloadCmBox();
			Stage stage = (Stage) accept.getScene().getWindow();
		    stage.close();
		}
	}
	
	public void cancel(ActionEvent event) {
		Stage stage = (Stage) cancel.getScene().getWindow();
	    stage.close();
	}
}
