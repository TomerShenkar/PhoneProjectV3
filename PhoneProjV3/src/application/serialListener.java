package application;
public interface serialListener {
	/**
	 * This method is an interface of the serialListener. 
	 * It executes the serialData method, that takes the byte array, turns it to string and adds it to the main string being processed.
	 * <p>Implemented in collectSerialData
	 * @param 
	 * @return None
	 */
	public void serialData(byte[] bytarr);
}

