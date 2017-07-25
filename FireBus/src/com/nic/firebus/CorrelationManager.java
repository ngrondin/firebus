package com.nic.firebus;

import java.util.HashMap;

public class CorrelationManager 
{
	protected HashMap<Integer, Message> messages;
	protected HashMap<Integer, ServiceRequestor> callbacks;
	
	protected static int nextCorrelation = 0;
	
	public CorrelationManager()
	{
		messages = new HashMap<Integer, Message>();
		callbacks = new HashMap<Integer, ServiceRequestor>();
	}
	
	public int getNextCorrelation()
	{
		return nextCorrelation++;
	}
	
	public void addMessage(int c, Message m)
	{
		messages.put(c, m);
	}
	
	public void addCallback(int c, ServiceRequestor sr)
	{
		callbacks.put(c, sr);
	}
	
	public void removeCorrelation(int c)
	{
		messages.remove(c);
		callbacks.remove(c);
	}
	
	public boolean hasMessage(int c)
	{
		return messages.containsKey(c);
	}
	
	public Message getMessage(int c)
	{
		return messages.get(c);
	}
}
