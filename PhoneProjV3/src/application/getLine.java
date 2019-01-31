package application;

import java.util.LinkedList;
import java.util.Queue;


public class getLine {

	public boolean linemode; //If true, add up every character until /r. If false, every character that comes in is it's separate line 
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
			if(linemode == true) {
				Save = Save + (char)raw[i];
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

