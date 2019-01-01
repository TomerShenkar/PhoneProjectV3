package application;
import java.util.Arrays;

import com.fazecast.jSerialComm.*;

public class SerialHandler {
	private static SerialPort [] ports;
	private static int portIndex;
	private String [] namesArray;
	private SerialListener listener;
	public SerialHandler()
	{
		ports = SerialPort.getCommPorts();
		portIndex = -1;
	}
	
	public String [] getSystemComPortNames()
	{		
		namesArray = new String[ports.length];
		for (int i = 0; i<ports.length;i++)
			namesArray[i] = ports[i].getSystemPortName();
		return namesArray;
	}
	
	public void portClose()
	{
		System.out.println("Closing " + portIndex);
		if (portIndex != -1)
			ports[portIndex].closePort();		
	}
	public boolean portOpen(String name)
	{
		boolean rc = false;
		for (int i=0;i<namesArray.length;i++)
		{
			if (name.equals(namesArray[i]))
			{
				portIndex = i;
				break;
			}
		}
		if (portIndex == -1)
			System.out.println("No port match");
		else
		{
			System.out.println("Index " + portIndex);
			ports[portIndex].addDataListener(new SerialPortDataListener() {
				   public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
				   public void serialEvent(SerialPortEvent event)
				   {
				   if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
				         return;
				      if (ports[portIndex].bytesAvailable() > 0)
				      {
					      byte[] newData = new byte[ports[portIndex].bytesAvailable()];
					      int numRead = ports[portIndex].readBytes(newData, newData.length);
					      if (numRead > 0)
					      {
					    	  byte [] slice = Arrays.copyOfRange(newData, 0, numRead);
					    	  if (listener != null)
					    		  listener.dataReceived(slice);
					      }			    	  
				      }
				   }
				});
			rc = ports[portIndex].openPort();
		}
		return rc;
	}
	
	public void portWrite(byte [] b)
	{
		ports[portIndex].writeBytes(b, b.length);
	}

	public void portWrite(String s)
	{
		byte [] b = s.getBytes();
		ports[portIndex].writeBytes(b, b.length);
	}
	
	public synchronized void setListener( SerialListener l ) {
		System.out.println("SerialHandler listener set");
		listener = l;
    }
    
    public synchronized void removeListener( SerialListener l ) {
    	listener = null;
    }}
