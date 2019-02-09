package application;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainController extends Main implements Initializable {
	// CLASS VERIABLES
	protected static serialListener sl = new collectSerialData();
	private SQLiteD sql = new SQLiteD();
	getLine GetLine = new getLine();

	// PORT VERIABLES
	String[] names = SH1.listOfPorts();
	int activePort = -1;
	static Queue<String> Addition = new LinkedList<String>();
	ObservableList<String> list;

	// INCOMING MESSAGE/CALL VERIABLES
	boolean isRing = false; //Made in order to disable multiple prints of same message
	boolean isClip = false; //Made in order to disable multiple prints of same message
	boolean nextIsMSG = false; // Made in order to allow print of incoming message

	// NUMBER VERIABLES
	String phoneNum = ""; // Typed number
	String incomingNumber; // Incoming caller's number

	// FXML VERIABLES
	@ FXML public ComboBox<String> comboBox; // CommPort display comboBox
	@ FXML TextArea textArea; // Main text area
	@ FXML TextField textFieldSMS; // SMS texting area
	@ FXML CheckBox cb; // SMS textBox area
	@ FXML AnchorPane AP; // CV.fxml's AnchorPane
	@ FXML Button openPort; // OpenPort Button
	@ FXML Button answerButton; //Answer Button
	@ FXML Button declineButton; //Decline Button
	
	// STATE VERIABLES
	protected static State phoneState = State.idle; // Defying the phone state

	protected static enum State { // Different phone states
		idle, typingNumber, typingMessage, dialing, ringing, duringCall, dialingFromContacts, incomingMessage,
		incomingMessageNumber;
	}
	
	@ Override
	public void initialize(URL arg0, ResourceBundle arg1) { // This method adds port names to the combo box
		for (int i = 0; i < names.length; i++) {
			if(names[i].startsWith("Sil")) {
				activePort = i;
				comboBox.setValue(names[i]);
			}
			comboBox.getItems().add(names[i]);
		}
		cb.setSelected(false);
		/*Image imageDecline = new Image(getClass().getResourceAsStream("endcall.png"));
		declineButton.setGraphic(new ImageView(imageDecline));
		Image imageAnswer = new Image(getClass().getResourceAsStream("answercall.png"));*/
	}

	public void addKeytoString(ActionEvent event) { // This method extracts the number from the keypad and sends it to
													// be added to the number string
		String number = ((Button) (event.getSource())).getText();
		phoneState = State.typingNumber;
		setNum(number);
	}

	public void setNum(String s) { // This method adds the keypad's clicked key to the number string
		// System.out.println(phoneNum);
		if(s != null) {
			phoneNum = phoneNum + s;
			setTextAreaNumber(phoneNum);
		}
	}

	public void setCheckBoxState(Boolean bool) {
		cb.setSelected(bool);
	}

	public void placeText() {
		String sms = textFieldSMS.getText();
		if(!sms.equals("") || phoneNum != null || !phoneNum.equals("")) {
			GetLine.linemode = false;
			SH1.writeString("AT+CMGS=" + "\"" + phoneNum + "\"", true);

			/*-
			 * try { 
			 Thread.sleep(500); //Not the way, figure out a way to send it without
			} 
			catch (InterruptedException e) { 
			 e.printStackTrace(); 
			} 
			GetLine.linemode = true;
			SH1.writeString(sms, true);
			byte[] endSMS = new byte[] { 26 };
			SH1.writeByte(endSMS);
			phoneState = State.typingMessage;
			cb.setSelected(false);*/
		}
	}

	public void answer(ActionEvent event) { // This method sets the answer key functions
		if(cb.isSelected()) { // If the checkbox is selected, send a text
			placeText();
		}
		else if(phoneState == State.typingNumber) { // If TypingNumber = call the number
			SH1.writeString("ATD" + phoneNum + ";", true);
			phoneState = State.dialing;
			setTextAreaState("Whatever");
		}
		else if(phoneState == State.ringing) { // If the phone is ringing = answer the call;
			SH1.writeString("ATA", true);
			phoneState = State.duringCall;
			setTextAreaState("Whatever");
		}
	}

	public void decline(ActionEvent event) { // This method ends the call
		if(phoneState == State.ringing || phoneState == State.duringCall || phoneState == State.dialing
				|| phoneState == State.dialingFromContacts || phoneState == State.idle) {
			SH1.writeString("ATH", true);
			phoneNum = "";
			phoneState = State.idle;
			setTextAreaState("Whatever");
		}
	}

	public void clrKey(ActionEvent event) { // This method clears the last number that is in the number string
		if(phoneNum.length() > 0) {
			phoneNum = phoneNum.substring(0, phoneNum.length() - 1);
			setTextAreaNumber(phoneNum);
			if(phoneNum.length() == 0) {
				phoneNum = "";
				phoneState = State.idle;
			}
		}
	}

	public void setTextAreaNumber(String s) { // This method displays the number being typed
		textArea.setText(s);
	}

	public void setTextAreaState(String incoming) { // This method displays any other messages needing display that
													// aren't the number

		if(phoneState == State.idle) {
			textArea.setText("End of call" + "\n"); // Clears the textArea
		}
		else if(phoneState == State.typingNumber) {
			// Do nothing, this is setTextAreaNumber
		}
		else if(phoneState == State.typingMessage) {
			textArea.appendText("\n" + "Sending " + textFieldSMS.getText() + " to " + detectNum(phoneNum));
		}
		else if(phoneState == State.dialing) {
			textArea.appendText("\n" + "Calling " + detectNum(incomingNumber));
		}
		else if(phoneState == State.ringing) {
			textArea.appendText("\n" + detectNum((incomingNumber)) + " is calling");
		}
		else if(phoneState == State.duringCall) {
			textArea.appendText("\n" + "In call with " + detectNum(incomingNumber));
		}
		else if(phoneState == State.dialingFromContacts) {
			textArea.appendText("\n" + "Calling " + detectNum(phoneNum.trim()));
		}
		else if(phoneState == State.incomingMessageNumber) {
			textArea.appendText("\n" + "Message from " + detectNum(incomingNumber) + ": ");
		}
		else if(phoneState == State.incomingMessage) {
			textArea.appendText(incoming);
		}
	}

	public void setTextArea(String display) { // This method displays any other messages needing display that aren't the number
		textArea.appendText("\n" + display + "\n");
	}

	private String detectNum(String temp) { // This method searches a specific number in the SQLite database
		String returnVal = sql.searchName(temp);
		if(returnVal == null) {
			returnVal = temp;
		}
		return returnVal;
	}

	public void clearTA(ActionEvent evevt) {
		phoneNum = "";
		textArea.setText("");
	}
	
	public String processMSG(String MSG) { // This method turns a +9725... number into a 05... number
		String[] MSGParts = MSG.split("\"");
		String NumberInternational = MSGParts[1];
		String NumberInterParts = NumberInternational.substring(4);
		String Number = "0" + NumberInterParts;
		return Number;
	}

	public void openContacts(ActionEvent event) { // This method opens the CV.fxml window
		try {
			FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/application/CV.fxml"));
			Parent root = (Parent) fxmlloader.load();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			Stage CVstage = new Stage();
			CVstage.setScene(scene);
			CVstage.show();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String Sim900Parse(String temp) { // This method parses the incoming command and displays it in readable
												// format
		if(temp != null) { // Text related
			if(nextIsMSG) {
				nextIsMSG = false;
				phoneState = State.incomingMessage;
				return (temp); // temp is the message.
			}

			else if(temp.startsWith("RING")) { // Phone Ringing related
				if (isRing == false) {
				isRing = true;
				return ("Ringing " + detectNum(incomingNumber));
				}
			}

			else if(temp.startsWith("+CLIP:")) { // Phone Ringing related
				if (isClip == false) {
				String[] parts = temp.split("\"");
				incomingNumber = parts[1];
				phoneState = State.ringing;
				isClip = true;
				return ("Call from " + detectNum(incomingNumber));
				}
			}

			else if(temp.startsWith("+COLP:")) { // Phone call related
				// Only when this phone is calling - Doesn't work for declined call from client
				String[] parts = temp.split("\"");
				incomingNumber = parts[1];
				phoneState = State.duringCall;
				return ("In call with " + detectNum(incomingNumber));
			}

			else if(temp.startsWith("NO CARRIER")) { // End of call related
				phoneState = State.idle;
				isRing = false;
				isClip = false; 
				return ("End of call");
			}

			else if(temp.startsWith("+CMT:")) { // Text related
				incomingNumber = processMSG(temp);
				nextIsMSG = true;
				phoneState = State.incomingMessageNumber;
				return (incomingNumber);
			}

			else if(temp.startsWith(">")) {
				GetLine.linemode = true;
				String sms = textFieldSMS.getText();
				SH1.writeString(sms, true);
				byte[] endSMS = new byte[] { 26 };
				SH1.writeByte(endSMS);
				phoneState = State.typingMessage;
				cb.setSelected(false);
				return ("Whatever");
			}

			else if(temp.startsWith("+CVCall:")) { // Only used for showing the dialed number when calling from
													// ContactsView
				String[] numberParts = temp.split(":");
				phoneNum = numberParts[1];
				phoneState = State.dialingFromContacts;
				String s = "ATD" + phoneNum.trim() + ";";
				SH1.writeString(s, true);
				return ("Calling " + detectNum(phoneNum.trim()));
			}

			else if(temp.startsWith("+CVText:")) { // Text from ContactsView
				String[] tempParts = temp.split(":");
				String[] numberParts = tempParts[1].split("\n");
				phoneNum = numberParts[0].trim();
				cb.setSelected(true);
				setTextArea(phoneNum);
			}
			
			else if(temp.startsWith("ERROR")) {
				setTextArea("Error");
			}
			
			else if(temp.startsWith("+CMGS")) {
				setTextArea("Message received");
			}
			
			else if(temp.startsWith("+CCLK:")) { // Clock related
				// Clock code
			}

			else {
				// None of the above
			}
		}
		return "";
	}

	public void openPort(ActionEvent event) { // This method opens the port and sets the listener for incoming commands
		// if (SH1.portOpener(comboBox.getSelectionModel().getSelectedIndex())) {
		if(SH1.portOpener(activePort)) {
			SH1.setListener(sl);
			SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
				@ Override
				protected Boolean doInBackground() throws Exception {
					int x = 10;
					while (x < 1000) {
						Thread.sleep(100);
						if(!Addition.isEmpty()) {
							GetLine.addRaw(Addition.remove());
						}
						while (!GetLine.getQ().isEmpty()) { // Emptying the queue.
							String temp = GetLine.getNext();
							System.out.println(temp);
							// Sim900Parse(temp);
							publish(Sim900Parse(temp)); // Complete line should be here
						}
					}
					return true;
				}

				@ Override
				protected void done() {

				}

				@ Override
				protected void process(List<String> chunks) {
					if(chunks.size() > 0) {
						for (int i = 0; i < chunks.size(); i++) {
							String OldestValue = chunks.get(i);
							if(!OldestValue.equals("")) {
								setTextAreaState(OldestValue);
							}
						}
						chunks.clear();
					}
				}
			};
			worker.execute();
			boolean disable = true; // Setting OpenPort button to be disabled from the command did not work.
			openPort.setDisable(disable);
		}
	}
}
