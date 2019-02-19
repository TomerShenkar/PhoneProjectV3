package application;

import java.util.LinkedList;
import java.util.Queue;


public class getLine {

	public boolean linemode; 
	private String Save;
	Queue<String> q;
	
	public getLine() {
		/*
		 * Constructor for getLine
		 */
		linemode = true;
		Save = "";
		q = new LinkedList<>();
	}
	
	public void addRaw(String s) {
		/*
		 * This method takes a string and adds it to the byte array being checked.
		 */
		byte[] sarr = s.getBytes();
		addRaw(sarr);
	}
	
	public void addRaw(byte[] raw) {
		/*
		 * This is where data coming from collectSerialData
		 * is added into command lines separated by a character feed (\r or 10 in bytes),
		 * which are then added into the queue.
		 * If the variable "linemode" is true, the command lines will be separated at the character feed.
		 * If it's set to false, every character that comes in is being added to the queue
		 * to be proccesed at the main controller.   
		 */
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
	
	public Queue<String> getQ(){
		/*
		 * This method is used to return the queue.
		 */
		return q;
	}
	
	public String getNext() {
		/*
		 * This method is used to return the latest value from the queue.
		 */
		return q.poll();
	}
}

