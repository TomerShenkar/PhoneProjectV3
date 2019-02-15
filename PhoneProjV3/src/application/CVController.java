package application;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CVController extends MainController{
	private SQLiteD sqld = new SQLiteD();
	
	private String[] arr = sqld.selectAll();
	private String number;
	static String selectedName;

	@FXML TextField tf;
	@FXML ComboBox<String> cmBox = new ComboBox<String>(); //Used for contact display 
	@FXML Button SDTomer;
	@FXML Button OpenConCreator;
	@FXML Button OpenConEditor;
	@FXML Button sendText;
    @FXML ListView<String> lw = new ListView<String>();
	
	public void initialize(URL arg0, ResourceBundle arg1)  {
		if(arr != null || arr.length > 0) {
			for(int i = 0; i<arr.length; i++) {
				cmBox.getItems().add(arr[i]);
				lw.getItems().add(arr[i]);
			}
		}
		
		lw.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if(newValue != null) {
		            String[] conParts = newValue.split("@");
		            String selectedConName = conParts[0];
		            number = conParts[1];
		            selectedName = selectedConName;
		            displayTF(selectedName);
		        }
			}	
		});
	}
	
	public void reloadCmBox() {
		cmBox.getItems().clear();
		arr = sqld.selectAll();
		cmBox.setItems(FXCollections.observableArrayList(Arrays.asList(arr)));
		//cmBox.getSelectionModel().selectFirst();
		
		lw.getItems().clear();
		lw.setItems(FXCollections.observableArrayList(Arrays.asList(arr)));
		//cmBox.getSelectionModel().selectFirst();
	}
	
	public void pickContact(ActionEvent event) {
		if(cmBox.getSelectionModel().getSelectedIndex() == -1) {
			return;
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
			String send = "+CVCall:" + number + "\n";
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
		reloadCmBox();
	}
	
	public void displayTF(String s) { //S being the value you want to display
		tf.setText(s);
	}
	
	public void sendText(ActionEvent event) {
		//Close this window, activate the SMS text field and set the receiver to be the chosen contact.
		if(number != null) {
			String send = "+CVText:" + number + "\n";
			sl.serialData(send.getBytes());
			((Node)(event.getSource())).getScene().getWindow().hide();

			/*Stage stage = (Stage) sendText.getScene().getWindow();
		    stage.close();*/
		}
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
    }
    catch (Exception e) {
        e.printStackTrace();
    }
}
}
