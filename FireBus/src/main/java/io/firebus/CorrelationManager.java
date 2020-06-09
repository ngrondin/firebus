package io.firebus;

import java.util.HashMap;
import java.util.logging.Logger;

import io.firebus.interfaces.CorrelationListener;

public class CorrelationManager extends Thread
{
	protected class CorrelationEntry
	{
		protected Message outboundMessage;
		protected MessageQueue inboundMessages;
		protected CorrelationListener correlationListener;
		protected long timeout;
		protected long expiry;
		
		public CorrelationEntry(long to)
		{
			timeout = to;
			expiry = System.currentTimeMillis() + to;
			inboundMessages = new MessageQueue(10);
		}		
	};
	
	private Logger logger = Logger.getLogger("io.firebus");
	protected HashMap<Integer, CorrelationEntry> entries;
	protected NodeCore nodeCore;
	protected boolean quit;
	
	protected static int nextCorrelation = 1;
	
	public CorrelationManager(NodeCore nc)
	{
		entries = new HashMap<Integer, CorrelationEntry>();
		nodeCore = nc;
		quit = false;
		setName("fbCorrMgr");
		start();
	}
	
	protected synchronized int getNextCorrelation()
	{
		return nextCorrelation++;
	}
	
	protected synchronized int createEntry(long to)
	{
		int c = getNextCorrelation();
		CorrelationEntry entry = new CorrelationEntry(to); 
		entries.put(c, entry);
		return c;
	}
	
	protected synchronized CorrelationEntry getEntry(int correlationId)
	{
		return entries.get(correlationId);
	}

	protected synchronized void removeEntry(int correlationId)
	{
		entries.remove(correlationId);
	}
	
	protected synchronized Integer[] getEntryKeyArray()
	{
		return entries.keySet().toArray(new Integer[0]);
	}
	
	public Message setListenerOnEntry(int correlationId, CorrelationListener cl, long timeout)
	{
		CorrelationEntry entry = getEntry(correlationId);
		Message message = null;
		if(entry != null)
		{
			synchronized(entry)
			{
				entry.timeout = timeout;
				entry.expiry = System.currentTimeMillis() + timeout;
				entry.correlationListener = cl;
				while(entry.inboundMessages.getMessageCount() > 0) 
				{
					entry.correlationListener.correlatedResponseReceived(entry.outboundMessage, entry.inboundMessages.pop());
				}				
			}
		}
		else
		{
			logger.severe("Correlation " + correlationId + " not found to set listener on");
		}
		return message;
	}

	public Message waitForResponse(int correlationId, int timeout)
	{
		CorrelationEntry entry = getEntry(correlationId);
		Message message = null;
		if(entry != null)
		{
			synchronized(entry)
			{
				entry.timeout = timeout;
				entry.expiry = System.currentTimeMillis() + timeout;
				while(System.currentTimeMillis() < entry.expiry  &&  entry.inboundMessages.getMessageCount() == 0)
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
			
				if(entry.inboundMessages.getMessageCount() > 0)
				{
					message = entry.inboundMessages.pop();
				}
			}
		}
		else
		{
			logger.severe("Correlation " + correlationId + " not found to wait for");
		}
		return message;
	}
	
	public Message sendAndWait(Message outMsg, int timeout)
	{
		int c = send(outMsg, null, timeout);
		return waitForResponse(c, timeout);
	}
	
	public int send(Message outMsg, int timeout)
	{
		return send(outMsg, null, timeout);
	}
	
	public int send(Message outMsg, CorrelationListener cl, long timeout)
	{
		int c = createEntry(timeout);
		CorrelationEntry entry = getEntry(c);
		entry.outboundMessage = outMsg;
		entry.correlationListener = cl;
		outMsg.setCorrelation(c);
		nodeCore.forkThenRoute(outMsg);
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
					logger.finer("Received Correlated message " + correlationId);
					if(entry.correlationListener == null)
					{
						entry.inboundMessages.push(inMsg);
					}
					else
					{
						entry.correlationListener.correlatedResponseReceived(entry.outboundMessage, inMsg);
					}
					entry.expiry = System.currentTimeMillis() + entry.timeout;
					entry.notify();
				}
			}
			else
			{
				String typeStr = (inMsg.getType() == Message.MSGTYPE_PROGRESS ? "progress" : (inMsg.getType() == Message.MSGTYPE_SERVICERESPONSE ? "final" : "other"));
				logger.fine("No correlation entry found for " + typeStr + " response from service " + inMsg.getSubject() + " (corr: " + correlationId + ")");
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
							entry.correlationListener.correlationTimedout(entry.outboundMessage);
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
	
	public void run()
	{
		while(!quit)
		{
			checkExpiredCalls();
			try
			{
				synchronized(this)
				{
					wait(100);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void close()
	{
		quit = true;
	}
}
