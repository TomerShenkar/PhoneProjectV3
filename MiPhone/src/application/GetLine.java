package application;

import java.util.LinkedList;
import java.util.Queue;

public class GetLine {
	public boolean linemode;
	Queue<String> q;
	// Constructor
	String tempString;	
	public GetLine ()
	{
		linemode = true;
		tempString = "";
		q = new LinkedList<>(); 
	}
	public void addRaw(byte [] rawData)
	{
		for (int i=0;i<rawData.length;i++)
		{
			if (linemode)
			{
				tempString += (char)rawData[i];
				if (rawData[i] == 10)
				{
					q.add(tempString);
					tempString = "";
				}				
			}
			else
			{
				tempString = "";
				tempString += (char)rawData[i];
				q.add(tempString);
				tempString = "";
			}
		}
	}
	public String getNext()
	{
		return q.poll();
	}
}
