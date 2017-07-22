package com.nic.firebus;

import java.util.ArrayList;

public class MessageQueue
{
	protected ArrayList<Message> messages;
	protected ArrayList<Long> processedID;
	protected ArrayList<Long> processedTime;
	
	public MessageQueue()
	{
		messages = new ArrayList<Message>();
		processedID = new ArrayList<Long>();
		processedTime = new ArrayList<Long>();
	}
	
	public void addMessage(Message m)
	{
		long ct = System.currentTimeMillis();
		while(processedTime.size() > 0  &&  processedTime.get(0) > ct - 60000)
		{
			processedID.remove(0);
			processedTime.remove(0);
		}
		
		if(!processedID.contains(m.getUniversalId()))
		{
			messages.add(m);
			processedID.add(m.getUniversalId());
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
