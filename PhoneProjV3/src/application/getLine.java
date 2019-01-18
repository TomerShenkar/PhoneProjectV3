package application;

import java.util.LinkedList;
import java.util.Queue;


public class getLine {

	public boolean linemode;
	private String Save;
	Queue<String> q;
	
	public getLine() {
		linemode = true;
		Save = "";
		q = new LinkedList<>();
	}
	
	public void addRaw(String s) {
		byte[] sarr = s.getBytes();
		addRaw(sarr);
	}
	
	public void addRaw(byte[] raw) {
		for(int i = 0; i<raw.length; i++) {
			Save = Save + (char)raw[i];
			if(linemode = true) {
				if(raw[i] == 10) { //Adding up until the \r
					//textArea_Debug.append(Save);
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
		return q;
	}
	
	public String getNext() {
		return q.poll();
	}
}

