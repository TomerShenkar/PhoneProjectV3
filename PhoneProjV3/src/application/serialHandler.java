package application;

import java.util.Arrays;

import com.fazecast.jSerialComm.*;
public class serialHandler {
	
	private SerialPort[] serialPorts;
	private String[] portNames;
	private static SerialPort chosenPort = null;
	private serialListener sl;
	private boolean isOpen = false;
	
	/**
	 * The constructor for serialHandler. Sets the port array list to the COMM ports.
	 * @param None
	 * @return None
	 */
	public serialHandler() {
		this.serialPorts = SerialPort.getCommPorts();
	}
	
	/**
	 * This method is responsible for getting the list of ports from the COMM port handler.
	 * @param None
	 * @return portNames
	 */
	String[] listOfPorts() {
	portNames = new String[serialPorts.length];
		for(int i = 0; i<serialPorts.length; i++) {
			portNames[i] = serialPorts[i].getDescriptivePortName();
		}
		return portNames;
	}
	
	/**
	 * This method is responsible for opening the chosen port.
	 * <p>This method is called from the main controller where a port has been selected,
	 * and is now opened by getting it's index and opening that port within the port array.
	 * <p>After that, the data listener is added, and any new data coming in is sent to serialData to be added to the string.
	 * <p>In this case, the byte array "Slice" is the actual bytes ready to be processed. 
	 * @param 
	 * @return true/false, according to the state of the port.
	 */
	public boolean portOpener(int index) {
		if(index<= serialPorts.length + 1 && index > -1) {
			chosenPort = serialPorts[index];
			if(chosenPort.openPort()) {
				isOpen = true;
				chosenPort.addDataListener(new SerialPortDataListener() {
					   public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
					   public void serialEvent(SerialPortEvent event) {
					   if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
					         return;
					      if (chosenPort.bytesAvailable() > 0) {
						      byte[] newData = new byte[chosenPort.bytesAvailable()];
						      int numRead = chosenPort.readBytes(newData, newData.length);
						      if (numRead > 0) {
						    	  byte[] slice = Arrays.copyOfRange(newData, 0, numRead);
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
	
	/**
	 * This method sets the listener for the serial port. 
	 * Used in main controller to set the listener for the chosen port.
	 * @param x
	 * @return None
	 */
	public void setListener(serialListener x) {
		sl = x;
	}
	
	/**
	 * This method closes the chosen port.
	 * @param None
	 * @return None
	 */
	public void portCloser() {
		if(chosenPort != null) {
			writeString("ATH", true);
			chosenPort.closePort();
		}
	}
	
	/**
	 * This method writs string messages to the COMM port. Any command wanted to be executed will go through this.
	 * <p>Depending on the cReturn value, a character line feed will be send at well. 
	 * @param 
	 * @return None
	 */
	public void writeString(String msg, boolean cReturn) {
		if(isOpen) {
			byte[] bytemsg = msg.getBytes();
			chosenPort.writeBytes(bytemsg, bytemsg.length);
			if(cReturn) {
				chosenPort.writeBytes(new byte[] {13}, 1);
			}
		}
	}
	
	/**
	 * THis method is like the one above, but this is used in order to execute one-byte or more commands, not entire lines.
	 * @param 
	 * @return None
	 */
	public void writeByte(byte[] msg) {
		chosenPort.writeBytes(msg, msg.length);
	}
}
