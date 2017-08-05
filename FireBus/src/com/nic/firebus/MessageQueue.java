package com.nic.firebus;

import java.util.ArrayList;

public class MessageQueue
{
	protected ArrayList<Message> messages;
	protected ArrayList<Integer> processedHash;
	protected ArrayList<Long> processedTime;
	
	public MessageQueue()
	{
		messages = new ArrayList<Message>();
		processedHash = new ArrayList<Integer>();
		processedTime = new ArrayList<Long>();
	}
	
	public void addMessage(Message m)
	{
		long ct = System.currentTimeMillis();
		while(processedTime.size() > 0  &&  processedTime.get(0) < ct - 60000)
		{
			processedHash.remove(0);
			processedTime.remove(0);
		}
		
		if(!processedHash.contains(m.hashCode()))
		{
			messages.add(m);
			processedHash.add(m.hashCode());
			processedTime.add(ct);
		}
	}
	
	public int getMessageCount()
	{
		return messages.size();
	}
	
	public Message getNextMessage()
	{
		return messages.get(0);
	}
		
	public void deleteNextMessage()
	{
		messages.remove(0);
	}

	
	
}
