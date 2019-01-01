package application;

import java.net.URL;
import java.util.List;
import java.util.Queue;
import java.util.ResourceBundle;

import javax.swing.SwingWorker;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class PhoneController implements Initializable{

	@FXML ComboBox<String> cbComPorts;
	@FXML TextArea txtLogger;
	@FXML Button btnOpen;
	@FXML Label lblStatus;
	@FXML Label lblIncoming;
	@FXML TextField txtNumber;
	@FXML TextField txtText;
	@FXML CheckBox chkSMS;
	private enum ePhoneStatus {IDLE,RINGING,ANSWERED,DIALLED,SMSIN};
	private ePhoneStatus phoneStatus;
	SerialHandler sh;
	GetLine gl;
//	protected static Queue<String> inQueue;
	protected static String addition = "";
	private String incoming = "";
	private String smsText = "";
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		sh = new SerialHandler();
		String [] pnames = sh.getSystemComPortNames();
		for (String s: pnames)
		{
			System.out.println(s);
			cbComPorts.getItems().add(s);
		}
		gl = new GetLine();
		setStatus(ePhoneStatus.IDLE);
	}
	public void openPort(ActionEvent ev)
	{
		if (sh.portOpen(cbComPorts.getValue().toString())) {
			btnOpen.setDisable(true);
			SerialListener sl = new CollectSerialData();
			sh.setListener(sl);
			SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
				@Override
				protected Boolean doInBackground() throws Exception {
					// Simulate doing something useful.
					int x = 100;
					boolean nextLineSMS = false;
					while (x < 1000) {
						Thread.sleep(100);
						// The type we pass to publish() is determined
						// by the second template parameter.
						if (!addition.equals("")) {
							String sq = addition;
					//		System.out.print(sq);
							addition = "";
							gl.addRaw(sq.getBytes());
						}
						String s = null;
						if (gl.members>0)
						{
							s = gl.getNext();
							System.out.print(s);
						}

						if (s != null)
						{
							System.out.println(s); 
							String sPublish = "";
							// parse the data and act accordingly
							if (nextLineSMS)
							{
								smsText = s;
								publish("sms");
								nextLineSMS = false;
								phoneStatus = ePhoneStatus.IDLE;
								publish("ps");
							}
							else if (s.startsWith("RING"))
							{
								System.out.println("Ringing");
								phoneStatus = ePhoneStatus.RINGING;
								publish("ps");
							}
							else if (s.startsWith("NO CARRIER"))
							{
//									phoneStatus = ePhoneStatus.IDLE;
								System.out.println("Stopped");
								phoneStatus =  ePhoneStatus.IDLE;
								publish("ps");
							}
							else if (s.startsWith("+CLIP:"))
							// +CLIP: "0545919886",129,"",,"Derek",0
							{
								String [] parts = s.split("\"");
								incoming = parts[1];
								System.out.println("Incoming from: " + parts[1]);
								publish("in");
							}
							else if (s.startsWith("+CMT:"))
							// +CMT: "number","contact","time"
							// msg
							{
								String [] parts = s.split("\"");
								incoming = parts[1];
								publish("in");
								nextLineSMS = true;
								phoneStatus = ePhoneStatus.SMSIN;
								publish("in");
							}
							publish(sPublish);
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
					// String mostRecentValue = chunks.get(chunks.size()-1);
					int size = chunks.size();
					for (int i = 0; i < chunks.size(); i++) {
						String c = chunks.get(i);
					//	txtLogger.appendText(c);
						if (c.equals("ps"))
						{
							String pss = phoneStatus.toString();
							Platform.runLater(new Runnable(){
								@Override
								public void run() {
									lblStatus.setText("Status: " + phoneStatus.toString());
								}
							});
						}
						else if (c.equals("in"))
						{
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									lblIncoming.setText(incoming);
								}		
							});
						}
						else if (c.equals("sms"))
						{
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									txtText.setText(smsText);
								}		
							});
							
						}
					}
					chunks.clear();
				}

			};

			worker.execute();
		} else {
			System.out.println("Failed to open serial port");
		}
	}
	public void setStatus(ePhoneStatus ps)
	{
		phoneStatus = ps;
		lblStatus.setText("Status: " + phoneStatus.toString());
	}
	
	public void setStatus(String ps)
	{
		setStatus(ePhoneStatus.valueOf(ps));
	}
	public void pressGreen(ActionEvent ev)
	{
		if (phoneStatus == ePhoneStatus.IDLE)
		{
			if (chkSMS.isSelected())
			{
				// send SMS
			}
			else
			{
				// dial number
				sh.portWrite("ATD"+txtNumber.getText()+";\r");
				setStatus(ePhoneStatus.DIALLED);
			}
		}
		else
		{
			sh.portWrite("ATA\r");
			setStatus( ePhoneStatus.ANSWERED);
		}
		
	}
	
	public void pressRed(ActionEvent ev)
	{
		sh.portWrite("ATH\r");
		setStatus( ePhoneStatus.IDLE);
	}
	
	public void addDigit(ActionEvent ev)
	{
		String k = txtNumber.getText() + ((Button)ev.getSource()).getText();
		txtNumber.setText(k);
	}
	
	public void delDigit(ActionEvent ev) {
		String k = txtNumber.getText();
		if (k.length() > 0)
			txtNumber.setText(k.substring(0, k.length()-1));		
	}
}
