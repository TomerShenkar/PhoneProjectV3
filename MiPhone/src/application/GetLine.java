package application;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;

public class GetLine {
	public boolean linemode;
	ArrayDeque<String> q;
	// Constructor
	String tempString;	
	public int members;
	public GetLine ()
	{
		linemode = true;
		tempString = "";
		q = new ArrayDeque<String>(); 
		members = 0;
	}
	public void addRaw(byte [] rawData)
	{
		for (int i=0;i<rawData.length;i++)
		{
			if (linemode)
			{
				tempString += (char)rawData[i];
				if (rawData[i] == 10) // 0x0a LF 0x0D CR
				{
					System.out.println("adding " + tempString );
					q.add(tempString);
					members++;
					tempString = "";
				}				
			}
			else
			{
				tempString = "";
				tempString += (char)rawData[i];
				q.add(tempString);
				members++;
				tempString = "";
			}
		}
	}
	public String getNext()
	{
		members--;
		return q.remove();
	}
}
