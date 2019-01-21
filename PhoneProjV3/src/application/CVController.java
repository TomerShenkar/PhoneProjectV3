package application;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class CVController extends MainController{
	private SQLiteD sqld = new SQLiteD();
	private String[] arr = sqld.selectAll();
	private String number;
	@FXML TextField tf;
	@FXML ComboBox<String> cmBox; //Used for contact display 
	@FXML Button SDTomer;
	
	public void initialize(URL arg0, ResourceBundle arg1)  {
		for(int i = 0; i<arr.length; i++) {
			cmBox.getItems().add(arr[i]);
		}
	}
	
	public void pickContact(ActionEvent event) {
		if(cmBox.getSelectionModel().getSelectedIndex() == -1) {
			displayTF("Error");
		}
		String selectedString = arr[cmBox.getSelectionModel().getSelectedIndex()];
		String[] conParts = selectedString.split("@");
		String conName = conParts[0];
		number = conParts[1];
		displayTF(conName);
	}
	
	public void placeCall(ActionEvent event) {
		if(number != null) {
			String send = "+TA:" + number + "\n";
			CVstage.close();
			sl.serialData(send.getBytes());
		}
	}
	
	public void speedDial(ActionEvent event) {
		String speedDialName = ((Button) (event.getSource())).getText();
		String speedDialNumber = sqld.searchSpecific("Number", "Name", speedDialName);
		number = speedDialNumber;
		placeCall(event);
	}
	
	public void displayTF(String s) { //S being the value you want to display
		tf.setText(s);
	}
}
