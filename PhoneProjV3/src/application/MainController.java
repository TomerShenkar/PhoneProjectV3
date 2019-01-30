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
	//CLASS VERIABLES
	protected static serialListener sl = new collectSerialData();
	private SQLiteD sql = new SQLiteD();
	getLine GetLine = new getLine();
    
    //PORT VERIABLES
	String[] names = SH1.listOfPorts();
	int activePort = -1;
	static Queue<String> Addition = new LinkedList<String>();
	ObservableList<String> list;

	//INCOMING MESSAGE/CALL VERIABLES
	boolean isRing = false; //Made in order to disable multiple prints of same message
	boolean isClip = false; //Made in order to disable multiple prints of same message
	boolean nextIsMSG = false; //Made in order to allow print of incoming message
	
	//NUMBER VERIABLES
	String phoneNum = ""; //Typed number
	String Number; //Incoming caller's number
	
	//FXML VERIABLES
	@FXML public ComboBox<String> comboBox; //CommPort display comboBox
	@FXML TextArea textArea; //Main text area
	@FXML TextField textFieldSMS; //SMS texting area
	@FXML CheckBox cb; //SMS textBox area
	@FXML AnchorPane AP; //CV.fxml's AnchorPane
	@FXML Button OpenPort; //OpenPort Button
	
	//STATE VERIABLES
	protected static State PhoneState = State.Idle; //Defying the phone state
	protected static enum State { //Different phone states
		Idle, TypingNumber, TypingMessage, Dialing, Ringing, DuringCall, DialingFromContacts, IncomingMessage, IncomingMessageNumber;
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) { //This method adds port names to the combo box
		for (int i = 0; i < names.length; i++) {
			if(names[i].startsWith("Sil")) {
				activePort = i;
				comboBox.setValue(names[i]);
			}
		comboBox.getItems().add(names[i]);
		}
		cb.setSelected(false);
	}
	
	public void addKeytoString(ActionEvent event) { //This method extracts the number from the keypad and sends it to be added to the number string
		String number = ((Button) (event.getSource())).getText();
		PhoneState = State.TypingNumber;
		setNum(number);
	}

	public void setNum(String s) { //This method adds the keypad's clicked key to the number string
		// System.out.println(phoneNum);
		if (s != null) {
			phoneNum = phoneNum + s;
			setTextAreaNumber(phoneNum);
		}
	}
	
	public void setCheckBoxState(Boolean bool) {
		cb.setSelected(bool);
	}
	
	public String getSMS() {
		return textFieldSMS.getText();
	}
	
	public void placeText() {
		String sms = getSMS();
		if(!sms.equals("")|| phoneNum != null || !phoneNum.equals("")) {
			SH1.writeString("AT+CMGS=" + "\"" + phoneNum + "\"", false);
			
			/*try {
				Thread.sleep(500); //Not the way, figure out a way to send it without sleep
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
		}
	}
	
	public void answer(ActionEvent event) { //This method sets the answer key functions 
		if(cb.isSelected()) { //If the checkbox is selected, send a text
			placeText();
		}
		else if(PhoneState == State.TypingNumber) { //If TypingNumber = call the number
				SH1.writeString("ATD" + phoneNum + ";", true);
				PhoneState = State.Dialing;
				setTextAreaState("Whatever");
			}
			else if(PhoneState == State.Ringing) { //If the phone is ringing = answer the call;
				SH1.writeString("ATA", true);
				PhoneState = State.DuringCall;
				setTextAreaState("Whatever");
			}
	}

	public void decline(ActionEvent event) { //This method ends the call
		if(PhoneState == State.Ringing || PhoneState == State.DuringCall || PhoneState == State.Dialing || PhoneState == State.DialingFromContacts) {
			SH1.writeString("ATH", true);
			phoneNum = "";
			PhoneState = State.Idle;
			setTextAreaState("Whatever");
		}
	}

	public void clrKey(ActionEvent event) { //This method clears the last number that is in the number string
		if (phoneNum.length() > 0) {
			phoneNum = phoneNum.substring(0, phoneNum.length() - 1);
			setTextAreaNumber(phoneNum);
			if (phoneNum.length() == 0) {
				phoneNum = "";
				PhoneState = State.Idle;
			}
		}
	}
	
	public void setTextAreaNumber(String s) { //This method displays the number being typed
		textArea.setText(s);
	}
	
	public void setTextAreaState(String incoming) { //This method displays any other messages needing display that aren't the number
		if(PhoneState == State.TypingNumber) {
			//Do nothing, this is setTextAreaNumber
		}
		
		else if(PhoneState == State.IncomingMessageNumber) {
			textArea.appendText("Incoming message from " + detectNum(incoming) + ": ");
		}
		
		else if(PhoneState == State.IncomingMessage) {
			textArea.appendText(incoming + "\n");
		}
		
		else if(PhoneState == State.TypingMessage) {
			textArea.appendText("Sending " + getSMS() + " to " + detectNum(phoneNum));
		}
		else if(PhoneState == State.Dialing) {
			textArea.appendText("Calling " + detectNum(phoneNum) + "\n");
		}
		else if(PhoneState == State.Ringing) {
			textArea.appendText(detectNum(phoneNum) + " Is calling \n");
		}
		
		else if(PhoneState == State.DuringCall) {
			textArea.appendText("In call with " + detectNum(Number) + "\n");
		}
		else if(PhoneState == State.DialingFromContacts) {
			textArea.appendText("Calling from contacts to " + detectNum(Number.trim()) + "\n");
		}
		
		else if(PhoneState == State.Idle) {
			textArea.appendText("Call Ended" + "\n");
			
		}
		
		/*Platform.runLater(new Runnable() {
		    public void run() {
		        textArea.appendText(incoming);
		    }
		});*/

	}
	
	public void setTextArea(String display) { //This method displays any other messages needing display that aren't the number
		textArea.appendText("\n" + display + "\n");
	}
	
	private String detectNum(String temp) { //This method searches a specific number in the SQLite database 
		String returnVal = sql.searchName(temp);
		if(returnVal == null) {
			returnVal = temp;
		}
		return returnVal;
	}

	public String processMSG(String MSG) { //This method turns a +9725... number into a 05... number 
		String[] MSGParts = MSG.split("\"");
		String NumberInternational = MSGParts[1];
		String NumberInterParts = NumberInternational.substring(4);
		String Number = "0" + NumberInterParts;
		return Number;
	}
	
	public void openContacts(ActionEvent event) { //This method opens the CV.fxml window
		try {
			FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/application/CV.fxml"));
			Parent root = (Parent) fxmlloader.load(); 
	        Scene scene = new Scene(root);
	        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
	        Stage CVstage = new Stage();
	        CVstage.setScene(scene);
	        CVstage.show();
			}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private String Sim900Parse(String temp) { //This method parses the incoming command and displays it in readable format
		if (temp != null) { 
			if (nextIsMSG) { //Text related
				PhoneState = State.IncomingMessage;
				nextIsMSG = false;
				return (temp);
			}

			else if (temp.startsWith("RING")) { //Phone Ringing related
					PhoneState = State.Ringing;
					return ("Ringing");
			}
			
			else if (temp.startsWith("+CLIP:")) { //Phone Ringing related
					String[] parts = temp.split("\"");
					Number = parts[1];
					PhoneState = State.Ringing;
					return ("Call from " + detectNum(Number));
			}
			
			else if(temp.startsWith("+COLP:")) { //Phone call related
				//Only when this phone is calling - Doesn't work for declined call from client 
				String[] parts = temp.split("\"");
				String Number = parts[1];
				PhoneState = State.DuringCall;
				return ("In call with " + detectNum(Number));
			}
			
			else if (temp.startsWith("NO CARRIER")) { //End of call related
				PhoneState = State.Idle;
				return ("End of call");
			}

			else if (temp.startsWith("+CCLK:")) { //Clock related
				// Clock code
			}
			
			else if(temp.startsWith("+CVCall:")) { //Only used for showing the dialed number when calling from ContactsView
				String[] numberParts = temp.split(":");
				Number = numberParts[1];
				PhoneState = State.DialingFromContacts;
				String s = "ATD" + Number.trim() + ";";
				SH1.writeString(s, true);
				return("Calling to " + detectNum(Number.trim()));
			}
			
			else if (temp.startsWith("+CMT:")) { //Text related
				String Number = processMSG(temp);
				PhoneState = State.IncomingMessageNumber;
				nextIsMSG = true;
				return (Number);
			}
			
			else if (temp.startsWith("+CVText:")) { //Text from ContactsView
				String[] tempParts = temp.split(":");
				String[] numberParts = tempParts[1].split("\n");
				phoneNum = numberParts[0].trim();
				if(cb.isSelected()) {
					setTextArea(phoneNum);
					placeText();
				}
			}
			
			else if(temp.startsWith(">")) { //Text related
				String sms = getSMS();
				SH1.writeString(sms, true);
				byte[] endSMS = new byte[] {26};
				SH1.writeByte(endSMS);
				PhoneState = State.TypingMessage;
				cb.setSelected(false);
				return("Whatever");
			}
			
			else {
				// None of the above
			}
		}
		return "";
	}
	
	public void openPort(ActionEvent event) { //This method opens the port and sets the listener for incoming commands
		//if (SH1.portOpener(comboBox.getSelectionModel().getSelectedIndex())) { 
		if (SH1.portOpener(activePort)) {
			SH1.writeString("ATE0", true	);
			SH1.setListener(sl);
			SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
				@Override
				protected Boolean doInBackground() throws Exception {
					int x = 10;
					while (x < 1000) {
						Thread.sleep(75);
						if (!Addition.isEmpty()) {
							GetLine.addRaw(Addition.remove());
						}
							while (!GetLine.getQ().isEmpty()) { //Emptying the queue.
								String temp = GetLine.getNext(); 
								System.out.println(temp);
								publish(Sim900Parse(temp)); //Complete line should be here
						}
					}
					return true;
				}
				
				@Override
				protected void done() {
					
				}
				
				@Override
				protected void process(List<String> chunks) {
					if (chunks.size() > 0) {
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
			OpenPort.setDisable(disable);
		}
	}
}
