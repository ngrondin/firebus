package com.nic.firebus;

import java.util.ArrayList;

public class MessageQueue
{
	protected ArrayList<Message> messages;
	
	public MessageQueue()
	{
		messages = new ArrayList<Message>();
	}
	
	public void addMessage(Message m)
	{
		messages.add(m);
	}
	
	public int getMessageCount()
	{
		return messages.size();
	}
	
	public Message getNextMessage()
	{
		return messages.get(0);
	}
	
	public void deleteNexInboundMessage()
	{
		messages.remove(0);
	}
	
	
}
