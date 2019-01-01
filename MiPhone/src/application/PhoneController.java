package application;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.SwingWorker;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
/*
 * SIM900 initialization
 * ATE0   no echo
 * AT+COLP=1  notification that remore answered or not
 *      +COLP: "number",129,"","name"  rejected
 *             "number:",129,0,"name  accepted
 * AT+CMGF=1  SMS on text mode   
 * AT+DDET=1  to read DTMF
 */
public class PhoneController implements Initializable{

	@FXML ComboBox<String> cbComPorts;
	@FXML TextArea txtLogger;
	@FXML Button btnOpen;
	@FXML Label lblStatus;
	@FXML Label lblIncoming;
	@FXML TextField txtNumber;
	@FXML TextField txtText;
	@FXML CheckBox chkSMS;
	@FXML MediaView mv;
	@FXML Button btnGreen;
	@FXML Button btnRed;

	private enum ePhoneStatus {IDLE,RINGING,ANSWERED,DIALLED,SMSIN,SMSOUT};
	private ePhoneStatus phoneStatus;
	SerialHandler sh;
	GetLine gl;
//	protected static Queue<String> inQueue;
	protected static String addition = "";
	private String incoming = "";
	private String smsText = "";
	private MediaPlayer mpAudioSingle,mpVideo;
	private Media mRing,mVideo;
	boolean callPlaying = false;
	boolean loopPlay = true;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		btnGreen.setDisable(true);
		btnRed.setDisable(true);
		
		sh = new SerialHandler();
		String [] pnames = sh.getSystemComPortNames();
		for (String s: pnames)
		{
			System.out.println(s);
			cbComPorts.getItems().add(s);
		}
		gl = new GetLine();
		setStatus(ePhoneStatus.IDLE);
		// set up the media player for incoming calls
		String a_name = new File("src/media/0743.mp3").getAbsolutePath();
		mRing = new Media(new File(a_name).toURI().toString());
		mpAudioSingle = new MediaPlayer(mRing);
		mpAudioSingle.setAutoPlay(false);
		// next bit for loop play
		if (loopPlay)
		{
			mpAudioSingle.setOnEndOfMedia(new Runnable () {
				@Override
				public void run() {
					System.out.println("End of audio");
					// rewind & play again
					mpAudioSingle.seek(Duration.ZERO);
				}			
			});		
		}
		// set up the media player for the Hands clip
		String v_name = new File("src/media/Hands.mp4").getAbsolutePath();
		mVideo = new Media(new File(v_name).toURI().toString());
		mpVideo = new MediaPlayer(mVideo);
		mv.setMediaPlayer(mpVideo);
		// make video fix the media view
		DoubleProperty width = mv.fitWidthProperty();
		DoubleProperty height = mv.fitHeightProperty();
		try {
		width.bind(Bindings.selectDouble(mv.sceneProperty(), "width"));
		} catch (Exception e)
		{
			System.out.println(e.toString());
		}
		try  {
		height.bind(Bindings.selectDouble(mv.sceneProperty(), "height"));
		} catch (Exception e)
		{
			System.out.println(e.toString());
		}		mpVideo.play();
		
		txtNumber.setText("0545919886");
		txtText.setText("blah");
	}
	public void openPort(ActionEvent ev)
	{
		if (sh.portOpen(cbComPorts.getValue().toString())) {
			btnOpen.setDisable(true);
			btnGreen.setDisable(false);
			btnRed.setDisable(false);
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
					//		System.out.println(s); 
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
							else if (s.startsWith(">"))
							{
								gl.linemode = true;
								sh.portWrite(txtText.getText());
								byte [] ctrlz = {0x1a};
								sh.portWrite(ctrlz);
							}
							else if (s.startsWith("RING"))
							{
								System.out.println("Ringing");
								phoneStatus = ePhoneStatus.RINGING;
								publish("ps");
								// kick off ringtone
								if (!callPlaying)
								{
									callPlaying = true;
									playaudio();
								}
							}
							else if (s.startsWith("NO CARRIER") || s.startsWith("+CMGS:"))
							{
//									phoneStatus = ePhoneStatus.IDLE;
								System.out.println("Stopped");
								phoneStatus =  ePhoneStatus.IDLE;
								publish("ps");
								callPlaying = false;
								stopaudio();
							}
							else if (s.startsWith("+CLIP:"))
							// +CLIP: "0545919886",129,"",,"Derek",0
							{
								String [] parts = s.split("\"");
								incoming = parts[1];
								System.out.println("Incoming from: " + parts[1]);
								publish("in");
							}
							else if (s.startsWith("+COLP:"))
						//     +COLP: "number",129,"","name"  rejected
						//             "number:",129,0,"name  accepted
								{
									String [] parts = s.split(",");
									if (parts.length == 4)
									{
										// rejected
										phoneStatus = ePhoneStatus.IDLE;
										publish("ps");
									}
									else
									{
										phoneStatus = ePhoneStatus.ANSWERED;
										publish("ps");
									}
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
							else if (s.startsWith("+DTMF:"))
								// +DTMF:n  where n is the ket
								// msg
							{
								String [] parts = s.trim().split(":");
								System.out.println(parts[1] + " pressed");
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
				gl.linemode = false;  // we are waiting for a single >
				String smcmd = "AT+CMGS=\""+txtNumber.getText()+"\"\r";
				sh.portWrite(smcmd);
				setStatus(ePhoneStatus.SMSOUT);
			}
			else
			{
				// dial number
				sh.portWrite("ATD"+txtNumber.getText()+";\r");
				setStatus(ePhoneStatus.DIALLED);
				playDialTones(txtNumber.getText());
			}
		}
		else
		{
			sh.portWrite("ATA\r");
			setStatus( ePhoneStatus.ANSWERED);
			callPlaying = false;
			stopaudio();
		}
		
	}
	
	public void pressRed(ActionEvent ev)
	{
		sh.portWrite("ATH\r");
		setStatus( ePhoneStatus.IDLE);
		callPlaying = false;
		stopaudio();
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
	/**
	 * @author David Henry
	 * @return void
	 * @throws Nothing
	 * @param mediaList 
	 */
	private void playMediaTracks(ObservableList<Media> mediaList) {
        if (mediaList.size() == 0)
            return;

        MediaPlayer mediaplayer = new MediaPlayer(mediaList.remove(0));
        mediaplayer.play();

        mediaplayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                playMediaTracks(mediaList);
            }
        });
    }
/**
 * @return void
 * @author David Henry
 * @param phone - Number to be dialled
 */
	private void playDialTones(String phone)
	{
		String prefix = "src/media/dtmf_";
		String suffix = ".wav";
		ObservableList<Media> mediaList = FXCollections.observableArrayList();
		for (int i=0;i<phone.length();i++)
		{
			mediaList.add(new Media(new File(new File(prefix + phone.substring(i, i+1) + suffix).getAbsolutePath()).toURI().toString()));
		}
        playMediaTracks(mediaList);
	}
	
	private void makePlaylist(String [] files)
	{
		ObservableList<Media> mediaList = FXCollections.observableArrayList();
		for (String s: files)
			mediaList.add(new Media(new File(new File(s).getAbsolutePath()).toURI().toString()));
        playMediaTracks(mediaList);
	}

	public void stopaudio()
	{
		mpAudioSingle.stop();
	}
	public void playaudio()
	{
		mpAudioSingle.seek(mpAudioSingle.getStartTime());
		mpAudioSingle.play();
	}
}
