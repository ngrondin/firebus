package com.nic.firebus;

import java.util.HashMap;
import java.util.logging.Logger;

import com.nic.firebus.interfaces.CorrelationListener;

public class CorrelationManager 
{
	protected class CorrelationEntry
	{
		protected Message outboundMessage;
		protected Message inboundMessage;
		protected CorrelationListener correlationListener;
		protected long expiry;
		
		public CorrelationEntry(Message om, CorrelationListener cl, int to)
		{
			outboundMessage = om;
			correlationListener = cl;
			expiry = System.currentTimeMillis() + to;
		}
	};
	
	private Logger logger = Logger.getLogger("com.nic.firebus");
	protected HashMap<Integer, CorrelationEntry> entries;
	protected NodeCore nodeCore;
	
	protected static int nextCorrelation = 1;
	
	public CorrelationManager(NodeCore nc)
	{
		entries = new HashMap<Integer, CorrelationEntry>();
		nodeCore = nc;
	}
	
	protected int getNextCorrelation()
	{
		return nextCorrelation++;
	}
	
	/*
	public Message synchronousCall(Message outMsg, int timeout)
	{
		int c = getNextCorrelation();
		outMsg.setCorrelation(c);
		CorrelationEntry entry = new CorrelationEntry(outMsg, null, timeout); 
		entries.put(c, entry);
		nodeCore.sendMessage(outMsg);
		return waitForResponse(c, timeout);
	}
*/
	
	public Message waitForResponse(int correlationId, int timeout)
	{
		CorrelationEntry entry = entries.get(correlationId);
		if(entry != null)
		{
			entry.expiry = System.currentTimeMillis() + timeout;
			while(System.currentTimeMillis() < entry.expiry  &&  entry.inboundMessage == null)
			{
				try
				{
					synchronized(this)
					{
						this.wait();
					}
				}
				catch(InterruptedException e)
				{
					logger.severe("Correlation synchronous call was interrupted : " + e.getMessage());
				}
			}
			
			if(entry.inboundMessage.getType() == Message.MSGTYPE_SERVICEPROGRESS)
			{
				entry.inboundMessage = null;
			}
			else
			{
				entries.remove(entry);
			}
			
			return entry.inboundMessage;
		}
		else
		{
			return null;
		}
	}
	
	public Message sendRequestAndWait(Message outMsg, int timeout)
	{
		int c = sendRequest(outMsg, null, timeout);
		return waitForResponse(c, timeout);
	}
	
	public int sendRequest(Message outMsg, int timeout)
	{
		return sendRequest(outMsg, null, timeout);
	}
	
	public int sendRequest(Message outMsg, CorrelationListener cl, int timeout)
	{
		int c = getNextCorrelation();
		outMsg.setCorrelation(c);
		CorrelationEntry e = new CorrelationEntry(outMsg, cl, timeout); 
		entries.put(c, e);
		nodeCore.sendMessage(outMsg);
		return c;
	}
	
	public void receiveResponse(Message inMsg)
	{
		int c = inMsg.getCorrelation();
		if(c != 0)
		{
			final CorrelationEntry e = entries.get(c);
			if(e != null)
			{
				logger.finer("Received Correlated Response");
				e.inboundMessage = inMsg;
				if(e.correlationListener != null)
				{
					final CorrelationListener cl = e.correlationListener;
					Thread t = new Thread(new Runnable() {
					    public void run() 
					    {
					    	cl.correlatedResponseReceived(e.outboundMessage, e.inboundMessage);
					    }
					});	
					t.start();
					entries.remove(c);
				}
				else
				{
					synchronized(this)
					{
						notify();
					}
				}
			}
		}
	}
	
	public void checkExpiredCalls()
	{
		long currTime = System.currentTimeMillis();
		Object[] ids = entries.keySet().toArray();
		for(int i = 0; i < ids.length; i++)
		{
			int c = (int)ids[i];
			CorrelationEntry e = entries.get(c);
			if(e.correlationListener != null && currTime > e.expiry)
			{
				final CorrelationListener cl = e.correlationListener;
				final Message m = e.outboundMessage;
				Thread t = new Thread(new Runnable() {
				    public void run() 
				    {
				    	cl.correlationTimedout(m);
				    }
				});	
				t.start();
				entries.remove(c);				
			}
			else
			{
				synchronized(this)
				{
					notify();
				}
			}
		}
	}
}
