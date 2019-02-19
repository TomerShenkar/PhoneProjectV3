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
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CVController extends MainController{
	private SQLiteD sqld = new SQLiteD();
	private String[] arr = sqld.selectAll();
	private String number;
	static String selectedName;

	@FXML TextField tf;
	@FXML Button OpenConCreator, OpenConEditor, sendText;
    @FXML ListView<String> lw = new ListView<String>();
	
	public void initialize(URL arg0, ResourceBundle arg1)  {
		/*
		 * This method is called every time the Contacts View window is opened.
		 * It inserts items into the ListView.
		 */
		if(arr != null || arr.length > 0) {
			lw.setItems(FXCollections.observableArrayList(Arrays.asList(arr)));
		}
		
		lw.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				/*	
				 * This method handles the selection of items from the list.
				 * newValue is the item selected. Because it's a string, the method
				 * splits it and only shows the name part.
				 */
				if(newValue != null) {
		            String[] conParts = newValue.split(": ");
		            String selectedConName = conParts[0];
		            number = conParts[1];
		            selectedName = selectedConName;
		            tf.setText(selectedName);
		        }
			}	
		});
	}
	
	public void reloadListView(ActionEvent event) {
		/*
		 * This method is needed because of GUI updating from different threads.
		 * When the button is clicked, the array "refreshes" and the list is updated. 
		 */
		arr = sqld.selectAll();
		lw.getItems().clear();
		if(arr != null || arr.length > 0) {
			lw.setItems(FXCollections.observableArrayList(Arrays.asList(arr)));
		}
		tf.setText("");
	}
	
	public void placeCall(ActionEvent event) {
		/*
		 * This method places a call to the selected contact.
		 * The variable "number" is set when selecting a contact.
		 * The method sends a string to the COMM port analyzer 
		 * which then is interpreted in the main controller, 
		 * and gets the outgoing number from the string, then calls that number.
		 * After the button is pressed, the call is placed and the window is hidden.
		 */
		if(number != null) {
			String send = "+CVCall:" + number + "\n";
			sl.serialData(send.getBytes());
			((Node)(event.getSource())).getScene().getWindow().hide();

		}
	}
	
	public void deleteCon(ActionEvent event) {
		/*
		 * This method deleted the selected contact, then refreshes the liswView.
		 */
		sqld.delete("Name", selectedName);
		reloadListView(event);
	}
	
	public void sendText(ActionEvent event) {
		/*
		 * This method places a text to the selected contact.
		 * The variable "number" is set when selecting a contact.
		 * The method sends a string to the COMM port analyzer 
		 * which then is interpreted in the main controller, 
		 * and gets the outgoing number from the string, then activated the text function with that number.
		 * After the button is pressed, the call is placed and the window is hidden.
		 */
		if(number != null) {
			String send = "+CVText:" + number + "\n";
			sl.serialData(send.getBytes());
			((Node)(event.getSource())).getScene().getWindow().hide();
		}
	}
	
	public void openConAdder(ActionEvent event) {
		/*
		 * This method opens the add contact FXML.
		 */
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
		/*
		 * This method opens the edit contact FXML.
		 */
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
