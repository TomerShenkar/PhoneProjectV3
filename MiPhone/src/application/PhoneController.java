package application;

import java.net.URL;
import java.util.List;
import java.util.Queue;
import java.util.ResourceBundle;

import javax.swing.SwingWorker;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class PhoneController implements Initializable{

	@FXML ComboBox<String> cbComPorts;
	@FXML TextArea txtLogger;
	@FXML Button btnOpen;
	@FXML Label lblStatus,lblIncoming;
	private enum ePhoneStatus {IDLE,RINGING,ANSWERED,DIALLED,SMSIN};
	private ePhoneStatus phoneStatus;
	SerialHandler sh;
	GetLine gl;
	protected static Queue<String> inQueue;
	
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
					while (x < 1000) {
						Thread.sleep(900);

						// The type we pass to publish() is determined
						// by the second template parameter.
						if (!inQueue.isEmpty()) {
							String sq = inQueue.remove();
							publish(sq);
							gl.addRaw(sq.getBytes());
							String s = gl.getNext();
							if (s != null)
							{
								// parse the data and act accordingly
								if (s.startsWith("RING"))
								{
									phoneStatus = ePhoneStatus.RINGING;
									System.out.println("Ringing");
								}
								if (s.startsWith("NO CARRIER"))
								{
									phoneStatus = ePhoneStatus.IDLE;
									System.out.println("Stopped");
								}
								else if (s.startsWith("+CLIP:"))
								// +CLIP: "0545919886",129,"",,"Derek",0
								{
									String [] parts = s.split("\"");
									System.out.println("Incoming from: " + parts[1]);
								}
							}
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
						txtLogger.appendText(chunks.get(i));
					}
				//	lblStatus.setText("Status: " + phoneStatus.toString());
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
	
	public void pressGreen(ActionEvent ev)
	{
		sh.portWrite("ATA\r");
		phoneStatus = ePhoneStatus.ANSWERED;
		
	}
	
	public void pressRed(ActionEvent ev)
	{
		sh.portWrite("ATH\r");
		phoneStatus = ePhoneStatus.IDLE;
	}

}
