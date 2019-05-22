package application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class Main extends Application {
	
	serialHandler SH1 = new serialHandler();
	
	/**
	 * This method starts up the primaryStage. It loads the first window.
	 */
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("/application/Main.fxml"));
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setTitle("SIM900");
			primaryStage.setScene(scene);
			primaryStage.setOnHiding(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent arg0) {
					SH1.portCloser(); 
					Platform.exit();
				}
			});
			primaryStage.show();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
	
}
