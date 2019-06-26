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
import javafx.util.Duration;
import javafx.animation.*;

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
	boolean isRing = false; // Made in order to disable multiple prints of same message
	boolean isClip = false; // Made in order to disable multiple prints of same message
	boolean nextIsMSG = false; // Made in order to allow print of incoming message
	boolean isCSPN = false;

	// NUMBER VERIABLES
	String phoneNum = ""; // Outgoing number
	String incomingNum; // Incoming caller's number

	// FXML VERIABLES
	@ FXML public ComboBox<String> comboBox; // CommPort display comboBox
	@ FXML TextArea textArea; // Main text area
	@ FXML TextField textFieldSMS; // SMS texting area
	@ FXML CheckBox cb; // SMS textBox area
	@ FXML AnchorPane AP; // CV.fxml's AnchorPane
	@ FXML Button openPort; // OpenPort Button
	@ FXML Button answerButton; // Answer Button
	@ FXML Button declineButton; // Decline Button
	@ FXML Button clearTA; //Clear textArea button
	@ FXML Button openContacts; //OpenContacts Button
	// STATE VERIABLES
	protected static State phoneState = State.idle; // Defying the phone state
	
	protected static enum State { // Different phone states
		idle, typingNumber, typingMessage, dialing, ringing, duringCall, dialingFromContacts, incomingMessage,
		incomingMessageNumber, busy, endOfCall;
	}

	@ Override
	/**
	 * This method is first called when this window starts. 
	 * It is also used to initialize the comboBox with the port names in it. It also looks for the specific port that this app uses.
	 */
	public void initialize(URL arg0, ResourceBundle arg1) { // This method adds port names to the combo box
		for (int i = 0; i < names.length; i++) {
			if(names[i].startsWith("Sil")) {
				activePort = i;
				comboBox.setValue(names[i]);
			}
			comboBox.getItems().add(names[i]);
		}
		cb.setSelected(false);
		//Disabling the contacts and answer button beofre opening the port prevents performing actions when the port hasn't been opened. 
		openContacts.setDisable(true); 
		answerButton.setDisable(true);
		
		clearTA.setOnAction(event -> { //Reset whatever's on the main screen after one second
			textArea.setText("End of call");
			
			if(phoneState == State.duringCall) {
				textArea.setText("End of call");
			}
			else
				textArea.setText("Ok");
			
			clearTA();
		});

	}
	
	/**
	 * This method extracts the number from the keypad and sends it to be added to the number string
	 * @param event
	 * @return None
	 */
	public void addKeytoString(ActionEvent event) { 
		String number = ((Button) (event.getSource())).getText();
		phoneState = State.typingNumber;
		setNum(number);
	}
	
	/**
	 * This method adds the keypad's clicked key to the number string.
	 * @param s
	 * @return
	 */
	public void setNum(String s) {
		
		if(s != null) {
			phoneNum = phoneNum + s;
			setTextAreaNumber(phoneNum);
		}
	}
	
	/**
	 * This method works in the following way:
	 * <p>First, a safety check is made to assure no blank texts are sent.
	 * <br>Then, lineMode is set to false, allowing a ">" symbol to come in, after sending the "AT+CMGS" command.
	 * <br>When the ">" symbol is recognized by the Sim900Parser, the actual message is sent to the recipient. 
	 * @param None
	 * @return None
	 */
	public void sendSMS() {
		String sms = textFieldSMS.getText();
		if(!sms.equals("") || phoneNum != null || !phoneNum.equals("")) {
			GetLine.linemode = false;
			SH1.writeString("AT+CMGS=" + "\"" + phoneNum + "\"", true);
		}
	}
	
	/**
	 * This is the function that takes care of the answer key.
	 * <p>The first if statement checks if the SMS checkBox is ticked. If it is, run the placeText method.
	 * <br>Second will check the phone state. If a number is being typed, call that number.
	 * <br>And third, if the phone state is ringing, answer the call.
	 * @param 
	 * @return None
	 */
	
	public void answer(ActionEvent event) { 
		if(cb.isSelected()) { 
			sendSMS();
		}
		else if(phoneState == State.typingNumber) {
			SH1.writeString("ATD" + phoneNum + ";", true);
			phoneState = State.dialing;
			setTextAreaState("Whatever");
		}
		else if(phoneState == State.ringing) {
			SH1.writeString("ATA", true);
			phoneState = State.duringCall;
			setTextAreaState("Whatever");
		}
		
		else {
			//None of the above
		}
	}
	
	/**
	 * This method takes care of declining/ending a call.
	 * <br>If the phone is ringing, during a call or dialing a number, end the call.
	 * <br>This method also sets the last incoming/outgoing number. Depending on who initiated the call.
	 * @param
	 * @return None
	 */
	
	public void decline(ActionEvent event) { 
		if(phoneState == State.ringing || phoneState == State.duringCall || phoneState == State.dialing
				|| phoneState == State.dialingFromContacts || phoneState == State.typingNumber) {
			SH1.writeString("ATH", true);
	
			setTextArea("End of call");
			phoneNum = "";
			isRing = false;
			isClip = false;
			clearTA();
			setTextAreaState("Whatever");
		}
		
		else {
			//None of the above
		}
	}
	
	
	/**
	 * This method deletes the last character from the typed phone number. 
	 * @param
	 * @return None
	 */
	public void clrKey(ActionEvent event) { 
		if(phoneNum.length() > 0) {
			phoneNum = phoneNum.substring(0, phoneNum.length() - 1);
			setTextAreaNumber(phoneNum);
			if(phoneNum.length() == 0) {
				phoneNum = "";
				phoneState = State.idle;
			}
		}
	}
	

	/**
	 * This method displays the number being typed
	 * @param
	 * @return None
	 */
	public void setTextAreaNumber(String s) { 
		phoneState = State.typingNumber;
		textArea.setText(s);
	}
	
	/**
	 * This method is the way of displaying data on the textArea.
	 * <p>
	 * It works the following way: For different phone states, perform different actions.
	 * <br>When idle: Don't display a thing
	 * <br>When sending a message: Display the sent message to the sent number.
	 * <br>When dialing a number: Display who you're calling.
	 * <br>When ringing: Display the caller id
	 * <br>While during a call: Display who you're in a call with 
	 * <br>When trying to reach a number that is buys at the moment: Display who's busy.
	 * <br>When calling a number from contacts (works in a different way than the regular call. 
	 * <br>When a message is coming in: Display who sent the message, and show it.
	 * @param
	 * @return None
	 */
	public void setTextAreaState(String incoming) { 
		if(phoneState == State.idle) {
			clearTA();
		}
		else if(phoneState == State.typingMessage) { // Outgoing text
			textArea.appendText("\n" + "Sending " + textFieldSMS.getText() + " to " + detectNum(phoneNum));
		}
		else if(phoneState == State.dialing) { // Outgoing call
			textArea.appendText("\n" + "Calling " + detectNum(phoneNum));
		}
		else if(phoneState == State.ringing) { // Incoming call
			textArea.appendText("\n" + detectNum((incomingNum)) + " is calling");
		}
		else if(phoneState == State.duringCall) { // Incoming call
			textArea.appendText("\n" + "On a call with " + detectNum(incomingNum));
			phoneNum = "";
		}
		else if(phoneState == State.busy) { // Incoming call
			textArea.appendText("\n" + detectNum(phoneNum) + " is busy");
		}
		else if(phoneState == State.dialingFromContacts) { // Outgoing call
			textArea.appendText("\n" + "Calling " + detectNum(phoneNum.trim())); //Trim over the last digit required in orfer to have seperated states 
		}
		else if(phoneState == State.incomingMessageNumber) { // Incoming text
			textArea.appendText("\n" + "New message from " + detectNum(incomingNum) + ": ");
		}
		else if(phoneState == State.incomingMessage) {
			textArea.appendText(incoming);
		}
		else if(phoneState == State.endOfCall) {
			textArea.appendText("\n" + "Call ended");
			clearTA();
		}
	}
	
	/**
	 * This method displays any other messages needing displaying that aren't state related. (e.g. "Message sent!", "Error" etc).
	 * @param
	 * @return None
	 */
	public void setTextArea(String display) { 
		textArea.appendText(display + "\n");
	}
	
	/**
	 * This method searches for a specific number in the SQL contacts database.
	 * @param
	 * @return search result from SQL database
	 */
	private String detectNum(String temp) { 
		return sql.searchName(temp);
	}
	
	/**
	 * This method is used to clear the textArea when it's "dirty". Used via a button.
	 * @param
	 * @return None
	 */
	public void clearTA() {
		PauseTransition pause = new PauseTransition(Duration.seconds(2));
		pause.setOnFinished(event -> textArea.setText(""));
		pause.play();
		phoneNum = "";
		//phoneState = State.idle;
		//setTextAreaState("Whatever");
	}

	/**
	 * This method processes an incoming SMS sender's phone number, by stripping it from it's international 
	 * numbering plan standard, leaving it with the area code and personal number, which can be searched in the SQL table.
	 * @param
	 * @return None
	 */
	public String processMSG(String MSG) { 
		String[] MSGParts = MSG.split("\"");
		String NumberInternational = MSGParts[1];
		String NumberInterParts = NumberInternational.substring(4);
		String Number = "0" + NumberInterParts;
		return Number;
	}
	/**
	 * This method opens the CV.FXML. It works by loading the fxml file from a folder,
	 * then loading it on the screen using the applicaions.css stylesheet. The fxml layout 
	 * is set onto a stage that is opened. 
	 * @param
	 * @return None
	 */
	public void openContacts(ActionEvent event) { // This method opens the CV.fxml window
		try {
			FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/application/CV.fxml"));
			Parent root = (Parent) fxmlloader.load();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			Stage CVstage = new Stage();
			CVstage.setTitle("Contacts View");
			CVstage.setScene(scene);
			CVstage.show();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method is responsible for parsing data coming in from the getLine queue, complete command string.
	 * <p>
	 * With the exception of "nextIsMSG"; ">"; "+CVCall"; "+CVText"; All of the commands below can be seen in the "See Also" part.
	 * <p>In the case of "nextIsMSG": When receiving a text message, 
	 * 	  the value of nextIsMSG will be set to true, so that the SMS message will go through without having a command.
	 * <p>In the case of ">": When sending an SMS message, the value of lineMode in getLine will be changed to false, 
	 *    allowing for each character to be sent as it's own string, and be proccesed here. When the ">" sign is detected,
	 *    the text message that is in the SMS textArea will be sent.
	 * <p>In the case of "+CVCall": When calling a contact from the contacts view area, a string will be added to the command queue,
	 *    containing the wanted phone number. In this method, that phone number will be rung.
	 * <p>In the case of "+CVText": When this command is detected in the parse method, the wanted phone number will be set and the 
	 *    text check box will be ticked. 
	 * @param
	 * @return Wanted result. Rarely used later on, because the display occurs through the state, that's set in each case. 
	 * @see <a href="SIM900_AT.pdf">https://www.espruino.com/datasheets/SIM900_AT.pdf</a>
	 */
	private String Sim900Parser(String cParse) {
		if(cParse != null) { 
			if(nextIsMSG) {
				nextIsMSG = false;
				phoneState = State.incomingMessage;
				return (cParse); // Temp being the message
			}

			else if(cParse.startsWith("RING")) { 
				if(isRing == false) {
					isRing = true;
					return ("Ringing");
				}
			}

			else if(cParse.startsWith("+CLIP:")) {
				if(isClip == false) {
					String[] parts = cParse.split("\"");
					incomingNum = parts[1];
					phoneState = State.ringing;
					isClip = true;
					return ("Call from " + detectNum(incomingNum));
				}
			}

			else if(cParse.startsWith("+COLP:")) {
				// Only when this phone is calling - Doesn't work for declined call from client
				String[] parts = cParse.split("\"");
				incomingNum = parts[1];
				phoneState = State.duringCall;
				return ("In call with " + detectNum(incomingNum));
			}

			else if(cParse.startsWith("NO CARRIER")) { //This will also set the lastNum when a call is finished.
				phoneState = State.endOfCall;
				isRing = false;
				isClip = false;
				return ("NO CARRIER");
			}

			else if(cParse.startsWith("+CMT:")) { 
				incomingNum = processMSG(cParse);
				nextIsMSG = true;
				phoneState = State.incomingMessageNumber;
				return (incomingNum);
			}

			else if(cParse.startsWith(">")) {
				GetLine.linemode = true;
				String sms = textFieldSMS.getText();
				SH1.writeString(sms, true);
				byte[] endSMS = new byte[] {26};
				SH1.writeByte(endSMS);
				phoneState = State.typingMessage;
				cb.setSelected(false);
				return (">");
			}

			else if(cParse.startsWith("+CVCall:")) { // Only used for showing the dialed number when calling from ContactsView
				String[] numberParts = cParse.split(":");
				phoneNum = numberParts[1];
				phoneState = State.dialingFromContacts;
				String s = "ATD" + phoneNum.trim() + ";";
				SH1.writeString(s, true);
				return ("Calling " + detectNum(phoneNum.trim()));
			}

			else if(cParse.startsWith("+CVText:")) { // Text from ContactsView
				String[] tempParts = cParse.split(":");
				String[] numberParts = tempParts[1].split("\n");
				phoneNum = numberParts[0].trim();
				cb.setSelected(true);
				setTextArea("Text: " + phoneNum);
			}

			else if(cParse.startsWith("BUSY") || cParse.startsWith("NO ANSWER")) { // Will display "connect readch ..." if one of two responses show
				SH1.writeString("ATH", true);
				phoneState = State.busy;
				return("BUSY");
			}
			
			else if(cParse.startsWith("NO DIALTONE")) {
				SH1.writeString("ATH", true);
				phoneState = State.busy;
				return("BUSY");
			}
			
			else if(cParse.startsWith("ERROR")) {
				setTextArea("Error");
			}

			else if(cParse.startsWith("+CMGS")) {
				textFieldSMS.clear();
				phoneNum = "";
				setTextArea("Message sent!");
				clearTA();
			}

			else {
				// None of the above
			}
		}
		return "";
	}
	
	/**
	 * This method is the means to update a GI from another thread. In this case, it is data coming in from the COMM port. 
	 * <p>First, the chosen port is opened, and it's listener is set.
	 * <p>GetLine starts accumulating the data and sending the output to "publish". 
	 * <br>"publish" then parses the data through the "Sim900Parser" method above and inserts the parsed data into a list called "chunks".
	 * All of that is done in the "doInBackground" part. 
	 * <p>"Chunks" is removed in "process" and the GUI is updated from there.
	 * @param
	 * @return None
	 */
	public void openPort(ActionEvent event) { // This method opens the port and sets the listener for incoming commands
		if(SH1.portOpener(activePort)) {
			SH1.setListener(sl);
			SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
				@ Override
				protected Boolean doInBackground() throws Exception {
					int x = 10;
					while (x < 1000) {
						Thread.sleep(100);
						if(!Addition.isEmpty()) { // When data comes in from collectSerialData, it gets added to GetLine
							GetLine.addRaw(Addition.remove());
						}
						while (!GetLine.getQ().isEmpty()) { // Emptying the queue from get line - full commands.
							String temp = GetLine.getNext();
							System.out.println(temp); // Check
							publish(Sim900Parser(temp)); 
						}
					}
					return true;
				}

				@ Override
				protected void done() {

				}

				@ Override
				protected void process(List<String> chunks) { //GUI updating done here
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
			openPort.setDisable(true); //When the port has been opened, allow the user to press on contacts and answer button
			openContacts.setDisable(false);
			answerButton.setDisable(false);
			textArea.appendText("Port opened succesfully");
			clearTA();
		}
	}
}
