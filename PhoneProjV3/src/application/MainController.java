package application;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.SwingWorker;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainController extends Main implements Initializable {

	private serialHandler SH = new serialHandler();
	private SQLiteD sql = new SQLiteD();
	int activePort;
	String phoneNum = "";
	@FXML TextArea t;
	@FXML public ComboBox<String> comboBox = new ComboBox<String>();
	@FXML Button OpenPort;
	@FXML AnchorPane AP;
	@FXML Button closeButton;
	@FXML private AnchorPane rootPane;
	protected static enum State {
		Idle, TypingNumber, TypingMessage, Dialing, Ringing, DuringCall;
	}

	protected static State PhoneState = State.Idle;
	boolean isRing = false; //Made in order to disable multiple prints of same message
	boolean isClip = false; //Made in order to disable multiple prints of same message
	boolean nextIsMSG = false;
	getLine GetLine = new getLine();
	static String Addition;
	String[] names = SH.listOfPorts();
	ObservableList<String> list;
	String Number; //Incoming caller's number
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		for (int i = 0; i < names.length; i++) {
			comboBox.getItems().add(names[i]);
		}
	}

	@FXML
	
	public void addKeytoString(ActionEvent event) {
		String t = ((Button) (event.getSource())).getText();
		PhoneState = State.TypingNumber;
		setNum(t);
	}

	public void setNum(String s) {
		// System.out.println(phoneNum);
		if (s != null) {
			phoneNum = phoneNum + s;
			setTextAreaNumber(phoneNum);
		}
	}

	public void answer(ActionEvent event) {
		if(PhoneState == State.TypingNumber) {
			SH.writeString("ATD" + phoneNum + ";", true);
			setTextArea("\n");
			setTextArea("Calling " + detectNum(phoneNum) + "\n");
			//setTextArea("\n");
		}
		else if(PhoneState == State.Ringing) {
			SH.writeString("ATA", true);
			setTextArea("In call with " + Number + "\n");
		}
	}

	public void decline(ActionEvent event) {
		SH.writeString("ATH", true);
		//setTextArea("\n");
		setTextArea("Bye");
		phoneNum = "";
		setTextArea(phoneNum);
		//setTextArea("\n");
	}

	public void clrKey(ActionEvent event) {
		if (phoneNum.length() > 0) {
			phoneNum = phoneNum.substring(0, phoneNum.length() - 1);
			setTextAreaNumber(phoneNum);
			if (phoneNum.length() == 0) {
				phoneNum = "";
				PhoneState = State.Idle;
			}
		}
	}

	public void setTextAreaNumber(String s) {
		t.setText(s);
	}

	public void setTextArea(String s) {
		//t.appendText("\r\n");
		t.appendText(s);
	}

	public void openPort(ActionEvent event) {
		if (SH.portOpener(comboBox.getSelectionModel().getSelectedIndex())) {
			serialListener sl = new collectSerialData();
			SH.setListener(sl);
			// SH.writeString("AT+CCLK?\r", true);
			SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
				@Override
				protected Boolean doInBackground() throws Exception {
					// Simulate doing something useful.
					int x = 10;
					while (x < 1000) {
						Thread.sleep(1000);
						// The type we pass to publish() is determined
						// by the second template parameter.
						if (Addition != null) {
							GetLine.addRaw(Addition);
							// System.out.print(OldestValue);

							while (!GetLine.getQ().isEmpty()) { //Empty the queue.
								String temp = GetLine.getNext(); 
								publish(Sim900Parse(temp)); //Complete line should be here
							}
							// publish(Addition);
							// System.out.print(Addition);
							// Addition = "";
						}
					}

					// Here we can return some object of whatever type
					// we specified for the first template parameter.
					// (in this case we're auto-boxing 'true').
					return true;
				}

				// Can safely update the GUI from this method.
				protected void done() {
				}

				@Override
				// Can safely update the GUI from this method.
				protected void process(List<String> chunks) {
					// Here we receive the values that we publish().
					// They may come grouped in chunks.
					if (chunks.size() > 0) {
						for (int i = 0; i < chunks.size(); i++) {
							String OldestValue = chunks.get(i);
							if(!OldestValue.equals("")) {
								setTextArea(OldestValue);
							}
						}
						chunks.clear();
					}
				}

			};
			worker.execute();
			boolean disable = true; // Setting OpenPort button to be disabled from the command did not work.
			OpenPort.setDisable(disable);
		}
	}
	
	private String detectNum(String temp) {
		if(sql.searchName(temp) == null) {
			return temp;
		}
		return sql.searchName(temp);
	}
	
	private String Sim900Parse(String temp) {
		if (temp != null) { //Text related
			if (nextIsMSG) {
				nextIsMSG = false;
				return ("The message is " + temp + "\n");
			}

			else if (temp.startsWith("RING")) { //Phone Ringing related
				if (isRing == false) {
					PhoneState = State.Ringing;
					isRing = true;
					return ("Ringing" + "\n");
				}
			}

			else if (temp.startsWith("+CLIP:")) { //Phone Ringing related
				if (isClip == false) {
					String[] parts = temp.split("\"");
					Number = parts[1];
					PhoneState = State.Ringing;
					isClip = true;
					return ("Call from " + detectNum(Number) + "\n");
				}
			}

			else if (temp.startsWith("NO CARRIER")) { //End of call related
				PhoneState = State.Idle;
				isRing = false;
				isClip = false;
				return ("End of call" + "\n");
			}

			else if (temp.startsWith("+CCLK:")) { //Clock related
				// Clock code
			}

			else if (temp.startsWith("+CMT:")) { //Text related
				String Number = processMSG(temp);
				nextIsMSG = true;
				return ("Text from " + Number + "\n");
			}

			else {
				// None of the above
			}
		}
		return "";
	}

	public static String processMSG(String MSG) { //This turns a +9725... number into a 05... number 
		String[] MSGParts = MSG.split("\"");
		String NumberInternational = MSGParts[1];
		String[] NumberInterParts = NumberInternational.split("+972");
		String Number = "0" + NumberInterParts[1];
		return Number;
	}
	
	public void openContacts(ActionEvent event) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("/application/CV.fxml"));
	        Scene scene = new Scene(root);
	        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
	        Stage stage = new Stage();
	        stage.setScene(scene);
	        stage.show();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
