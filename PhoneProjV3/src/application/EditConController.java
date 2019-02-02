package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditConController extends CVController{
	
	@FXML TextField nameField;
	@FXML TextField numberField;
	@FXML Button accept;
	@FXML Button cancel;
	
	private SQLiteD sqld = new SQLiteD();
	private CVController cvc = new CVController();
	
	public void editCon(ActionEvent event) {
		String searchName = cvc.getSelectedName();
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
		Stage stage = (Stage) cancel.getScene().getWindow();
	    stage.close();
	}
}
