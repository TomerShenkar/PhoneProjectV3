package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class CVController extends MainController{
	private SQLiteD sqld = new SQLiteD();
	private String[] arr = sqld.selectAll();
	private String number;
	@FXML TextField tf;
	@FXML ComboBox<String> cmBox;
	
	public void fillCM(ActionEvent Event)  {
		for(int i = 0; i<arr.length; i++) {
			//System.out.println(arr[i]);
			cmBox.getItems().add(arr[i]);
			pickContact();
		}
	}
	
	public void pickContact() {
		String tmp = arr[cmBox.getSelectionModel().getSelectedIndex()];
		String[] conParts = tmp.split("@");
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
