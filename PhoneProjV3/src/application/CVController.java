package application;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CVController extends MainController{
	private SQLiteD sqld = new SQLiteD();
	private String[] arr = sqld.selectAll();
	private String number;
	@FXML TextField tf;
	@FXML ComboBox<String> cmBox = new ComboBox<String>(); //Used for contact display 
	@FXML Button SDTomer;
	@FXML Button OpenConCreator;
	@FXML Button OpenConEditor;
	static String selectedName;

	public void initialize(URL arg0, ResourceBundle arg1)  {
		if(arr != null || arr.length > 0) {
			for(int i = 0; i<arr.length; i++) {
				cmBox.getItems().add(arr[i]);
			}
		}
	}
	
	public void pickContact(ActionEvent event) {
		if(cmBox.getSelectionModel().getSelectedIndex() == -1) {
			displayTF("Error");
		}
		String selectedString = arr[cmBox.getSelectionModel().getSelectedIndex()];
		String[] conParts = selectedString.split("@");
		String selectedConName = conParts[0];
		number = conParts[1];
		selectedName = selectedConName;
		displayTF(selectedName);
	}
	
	public String getSelectedName() {
		return selectedName;
	}

	public void placeCall(ActionEvent event) {
		if(number != null) {
			String send = "+TA:" + number + "\n";
			sl.serialData(send.getBytes());
			((Node)(event.getSource())).getScene().getWindow().hide();
		}
	}
	
	public void speedDial(ActionEvent event) {
		String speedDialName = ((Button) (event.getSource())).getText();
		String speedDialNumber = sqld.searchSpecific("Number", "Name", speedDialName);
		number = speedDialNumber;
		placeCall(event);
	}
	
	public void deleteCon(ActionEvent event) {
		sqld.delete("Name", selectedName);
		((Node)(event.getSource())).getScene().getWindow().hide();
	}
	
	public void displayTF(String s) { //S being the value you want to display
		tf.setText(s);
	}
	
	public void openConAdder(ActionEvent event) {
        try {
        	FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/application/AddCon.fxml"));
			Parent root = (Parent) fxmlloader.load(); 
	        Scene scene = new Scene(root);
	        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
	    	Stage addConStage = new Stage();
	        addConStage.setScene(scene);
	        addConStage.show();
            ((Node)(event.getSource())).getScene().getWindow().hide();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
}

public void openConEditer(ActionEvent event) {
    try {
    	FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/application/EditCon.fxml"));
		Parent root = (Parent) fxmlloader.load(); 
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
    	Stage addConStage = new Stage();
        addConStage.setScene(scene);
        addConStage.show();
        ((Node)(event.getSource())).getScene().getWindow().hide();
    }
    catch (Exception e) {
        e.printStackTrace();
    }
}
}
