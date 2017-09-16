package com.nic.firebus;

import java.util.ArrayList;
import java.util.logging.Logger;

public class MessageQueue
{
	private Logger logger = Logger.getLogger("com.nic.firebus");
	protected ArrayList<Message> messages;
	protected ArrayList<Long> processedIds;
	protected ArrayList<Long> processedTime;
	
	public MessageQueue()
	{
		messages = new ArrayList<Message>();
		processedIds = new ArrayList<Long>();
		processedTime = new ArrayList<Long>();
	}
	
	public void addMessage(Message m)
	{
		long ct = System.currentTimeMillis();
		while(processedTime.size() > 0  &&  processedTime.get(0) < ct - 60000)
		{
			processedIds.remove(0);
			processedTime.remove(0);
		}
		
		long Id = (((long)m.getOriginatorId()) << 32) | ((long)m.getid());
		if(!processedIds.contains(Id))
		{
			messages.add(m);
			processedIds.add(Id);
			processedTime.add(ct);
		}
		else
		{
			logger.fine("Dropped echo message");
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
