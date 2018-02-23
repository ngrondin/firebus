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
	
	protected synchronized int getNextCorrelation()
	{
		return nextCorrelation++;
	}
	
	protected synchronized CorrelationEntry getEntry(int correlationId)
	{
		return entries.get(correlationId);
	}

	protected synchronized void putEntry(int correlationId, CorrelationEntry entry)
	{
		entries.put(correlationId, entry);
	}

	protected synchronized void removeEntry(int correlationId)
	{
		entries.remove(correlationId);
	}
	
	protected synchronized Integer[] getEntryKeyArray()
	{
		return entries.keySet().toArray(new Integer[0]);
	}

	public Message waitForResponse(int correlationId, int timeout)
	{
		CorrelationEntry entry = getEntry(correlationId);
		Message responseMessage = null;
		if(entry != null)
		{
			synchronized(entry)
			{
				entry.expiry = System.currentTimeMillis() + timeout;
				while(System.currentTimeMillis() < entry.expiry  &&  entry.inboundMessage == null)
				{
					try
					{
						entry.wait();
					}
					catch(InterruptedException e)
					{
						logger.severe("Correlation wait was interrupted : " + e.getMessage());
					}
				}
	
				if(entry.inboundMessage != null)
				{
					responseMessage = entry.inboundMessage;
					if(entry.inboundMessage.getType() == Message.MSGTYPE_SERVICEPROGRESS)
					{
						entry.inboundMessage = null;
					}
					else
					{
						removeEntry(correlationId);	
					}
				}
				else
				{
					removeEntry(correlationId);	
				}
			}
		}		
		return responseMessage;
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
		putEntry(c, e);
		nodeCore.sendMessage(outMsg);
		return c;
	}
	
	public void receiveResponse(Message inMsg)
	{
		int correlationId = inMsg.getCorrelation();
		if(correlationId != 0)
		{
			CorrelationEntry entry = getEntry(correlationId);
			if(entry != null)
			{
				synchronized(entry)
				{
					logger.finer("Received Correlated Response");
					entry.inboundMessage = inMsg;
					if(entry.correlationListener != null)
					{
						final CorrelationListener cl = entry.correlationListener;
						final Message oMsg = entry.outboundMessage;
						final Message iMsg = entry.inboundMessage;
						Thread t = new Thread(new Runnable() {
						    public void run() 
						    {
						    	cl.correlatedResponseReceived(oMsg, iMsg);
						    }
						});	
						t.start();
						removeEntry(correlationId);
					}
					else
					{
						entry.notify();
					}
				}
			}
		}
	}
	
	public void checkExpiredCalls()
	{
		long currTime = System.currentTimeMillis();
		Integer[] ids = getEntryKeyArray();
		for(int i = 0; i < ids.length; i++)
		{
			CorrelationEntry entry = getEntry(ids[i]);
			if(entry != null)
			{
				synchronized(entry)
				{
					if(currTime > entry.expiry)
					{
						logger.finer("Correlation " + ids[i] + " has expired");
						if(entry.correlationListener != null)
						{
							final CorrelationListener cl = entry.correlationListener;
							final Message m = entry.outboundMessage;
							Thread t = new Thread(new Runnable() {
							    public void run() 
							    {
							    	cl.correlationTimedout(m);
							    }
							});	
							t.start();
							removeEntry(ids[i]);
						}
						else
						{
							entry.notify();
						}					
					}
				}
			}
		}
	}
}
