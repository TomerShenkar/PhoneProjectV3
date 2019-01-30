package application;

import java.util.Arrays;

import com.fazecast.jSerialComm.*;
public class serialHandler {
	
	private SerialPort[] serialPorts;
	private String[] portNames;
	private static SerialPort chosenPort = null;
	private serialListener sl;
	
	public serialHandler() {
		this.serialPorts = SerialPort.getCommPorts();
	}
	
	String[] listOfPorts() {
	portNames = new String[serialPorts.length];
		for(int i = 0; i<serialPorts.length; i++) {
			portNames[i] = serialPorts[i].getDescriptivePortName();
		}
		return portNames;
	}
	
	public boolean portOpener(int index) {
		if(index<= serialPorts.length + 1 && index > -1) {
			chosenPort = serialPorts[index];
			if(chosenPort.openPort()) {
				chosenPort.addDataListener(new SerialPortDataListener() {
					   public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
					   public void serialEvent(SerialPortEvent event)
					   {
					   if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
					         return;
					      if (chosenPort.bytesAvailable() > 0)
					      {
						      byte[] newData = new byte[chosenPort.bytesAvailable()];
						      int numRead = chosenPort.readBytes(newData, newData.length);
						      if (numRead > 0)
						      {
						    	  byte [] slice = Arrays.copyOfRange(newData, 0, numRead);
						    	  sl.serialData(slice);
						      }			    	  
					      }
					   }
				});
				
				return true;
			}
			else
				return false;
		}
		else
			return false;
	}
	
	public void setListener(serialListener x) {
		sl = x;
	}
	
	public void portCloser() {
		if(chosenPort != null) {
			chosenPort.closePort();
		}
	}
	
	public void writeString(String msg, boolean cReturn) {
		byte[] bytemsg = msg.getBytes();
		chosenPort.writeBytes(bytemsg, bytemsg.length);
		if(cReturn) {
			chosenPort.writeBytes(new byte[] {13}, 1);
		}
	}
	
	public void writeByte(byte[] msg) {
		chosenPort.writeBytes(msg, msg.length);
	}
}
