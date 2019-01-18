package application;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class CVController extends MainController{
	private SQLiteD sqld = new SQLiteD();
	private String[] arr = sqld.selectAll();
	private String number;
	@FXML TextField tf;
	@FXML ComboBox<String> cmBox; //Used for contact display 
	
	public void initialize(URL arg0, ResourceBundle arg1)  {
		for(int i = 0; i<arr.length; i++) {
			//System.out.println(arr[i]);
			cmBox.getItems().add(arr[i]);
			pickContact();
		}
	}
	
	public void pickContact() {
		int selectedInt = cmBox.getSelectionModel().getSelectedIndex();
		String selectedString = arr[selectedInt];
		String[] conParts = selectedString.split("@");
		String conName = conParts[0];
		number = conParts[1];
		displayTF(conName);
	}
	
	public void placeCall(ActionEvent event) {
		if(number != null) {
			phoneNum = number;
			answer(event);
		}
	}
	
	public void displayTF(String s) { //S being the value you want to display
		tf.setText(s);
	}
}
