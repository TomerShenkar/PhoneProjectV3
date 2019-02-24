package application;

import java.util.LinkedList;
import java.util.Queue;


public class getLine {

	public boolean linemode; 
	private String Save;
	Queue<String> q;
	
	/**
	 * This method is the constructor for getLine.
	 * @param None
	 * @return None
	 */
	public getLine() {
		linemode = true;
		Save = "";
		q = new LinkedList<>();
	}
	
	/**
	 * This method takes a string and adds it to the byte array being checked.
	 * @param s
	 * @return None
	 */
	public void addRaw(String s) {
		byte[] sarr = s.getBytes();
		addRaw(sarr);
	}
	
	/**
	 * This is where data coming from collectSerialData
	 * is added into command lines separated by a character feed (\r or 10 in bytes),
	 * which are then added into the queue.
	 * If the variable "linemode" is true, the command lines will be separated at the character feed.
	 * If it's set to false, every character that comes in is being added to the queue
	 * to be proccesed at the main controller.   
	 * @param raw
	 * @return None
	 */
	public void addRaw(byte[] raw) {
		for(int i = 0; i<raw.length; i++) {
			if(linemode == true) {
				Save = Save + (char)raw[i];
				if(raw[i] == 10) { //Adding up until the \r
					q.add(Save);
					Save = "";
				}
			}
			else {
				Save = "";
				Save += (char)raw[i];
				q.add(Save);
				Save = "";
			}
		} 
	}
	
	/**
	 * This method is used to return the Queue 
	 * @param None
	 * @return q
	 */
	public Queue<String> getQ(){
		return q;
	}
	
	/**
	 * This method is used to return the top value from the queue.
	 * @param None
	 * @return q.poll();
	 */
	public String getNext() {
		return q.poll();
	}
}

