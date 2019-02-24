package application;

public class collectSerialData extends MainController implements serialListener {
	/** 
	 * This method takes the new data coming in from the COMM port.
	 * In order for it to not lose any data, any character coming in
	 * from the port is immediately added to the string, which then gets processed later on.
	 * @param event
	 * @return None
	 */
	public void serialData(byte[] rawData) { 
		Addition.add(new String(rawData));
	}		
}

