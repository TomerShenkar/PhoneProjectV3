package application;

public class collectSerialData extends MainController implements serialListener{

	public void serialData(byte[] rawData) { //Processing the data
		/*
		 * This method takes the new data coming in from the COMM port.
		 * In order for it to not lose any data, any character coming in
		 * from the port is immediately added to the string, which then gets proccesed later on.
		 */
		Addition.add(new String(rawData));
	}		
}

