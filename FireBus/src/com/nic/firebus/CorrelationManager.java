package com.nic.firebus;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import com.nic.firebus.interfaces.ServiceRequestor;

public class CorrelationManager 
{
	protected class CorrelationEntry
	{
		protected Message outboundMessage;
		protected Message inboundMessage;
		protected ServiceRequestor serviceRequestor;
		protected long expiry;
		
		public CorrelationEntry(Message om, ServiceRequestor sr, int to)
		{
			outboundMessage = om;
			serviceRequestor = sr;
			expiry = System.currentTimeMillis() + to;
		}
	};
	
	private Logger logger = Logger.getLogger(CorrelationManager.class.getName());
	protected HashMap<Integer, CorrelationEntry> entries;
	protected MessageQueue outboundQueue;
	
	protected static int nextCorrelation = 1;
	
	public CorrelationManager(MessageQueue oq)
	{
		entries = new HashMap<Integer, CorrelationEntry>();
		outboundQueue = oq;
	}
	
	public int getNextCorrelation()
	{
		return nextCorrelation++;
	}
	
	public Message synchronousCall(Message outMsg, int timeout)
	{
		int c = getNextCorrelation();
		outMsg.setCorrelation(c);
		CorrelationEntry e = new CorrelationEntry(outMsg, null, timeout); 
		entries.put(c, e);
		outboundQueue.addMessage(outMsg);
		int time = 0;
		while(time < timeout  &&  e.inboundMessage == null)
		{
			try{Thread.sleep(10);} catch(Exception err){}
			time += 10;
		}
		entries.remove(e);
		return e.inboundMessage;
	}
	
	public void asynchronousCall(Message outMsg, ServiceRequestor sr, int timeout)
	{
		int c = getNextCorrelation();
		outMsg.setCorrelation(c);
		CorrelationEntry e = new CorrelationEntry(outMsg, sr, timeout); 
		entries.put(c, e);
		outboundQueue.addMessage(outMsg);
	}
	
	public void receiveResponse(Message inMsg)
	{
		int c = inMsg.getCorrelation();
		CorrelationEntry e = entries.get(c);
		if(e != null)
		{
			logger.fine("Received Correlated Response");
			e.inboundMessage = inMsg;
			if(e.serviceRequestor != null)
			{
				final byte[] pl = inMsg.getPayload();
				final ServiceRequestor sr = e.serviceRequestor;
				Thread t = new Thread(new Runnable() {
				    public void run() 
				    {
				    	sr.requestCallback(pl);
				    }
				});	
				t.start();
				entries.remove(e);
			}
		}
	}
	
	public void houseKeeping()
	{
		long currTime = System.currentTimeMillis();
		Iterator<Integer> it = entries.keySet().iterator();
		while(it.hasNext())
		{
			CorrelationEntry e = entries.get(it.next());
			if(e.serviceRequestor != null && currTime > e.expiry)
			{
				final ServiceRequestor sr = e.serviceRequestor;
				Thread t = new Thread(new Runnable() {
				    public void run() 
				    {
				    	sr.requestTimeout();
				    }
				});	
				t.start();
				entries.remove(e);				
			}
		}
	}
}
