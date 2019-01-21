package application;

public class collectSerialData extends MainController implements serialListener{

	public void serialData(byte[] rawData) { //Processing the data 
		Addition.add(new String(rawData));
	}
		
}

