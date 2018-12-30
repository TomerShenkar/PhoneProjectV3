package application;

import java.util.Queue;

public class CollectSerialData extends PhoneController implements SerialListener {
	private static final long serialVersionUID = 1L;
	public void dataReceived( byte [] raw )
	 {
		 String s = new String(raw);
		System.out.print(s); 	
	//	inQueue.add(s);
		addition += s;
	 }	
}