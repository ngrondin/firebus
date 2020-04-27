package io.firebus;

import java.util.HashMap;
import java.util.logging.Logger;

import io.firebus.interfaces.CorrelationListener;

public class CorrelationManager extends Thread
{
	protected class CorrelationEntry
	{
		protected Message outboundMessage;
		protected Message progressMessage;
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
				while(System.currentTimeMillis() < entry.expiry  &&  entry.inboundMessage == null  &&  entry.progressMessage == null)
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
					logger.finer("Removing correlation entry " + correlationId + " as response received");
					removeEntry(correlationId);	
				}
				else if(entry.progressMessage != null)
				{
					responseMessage = entry.progressMessage;
					entry.progressMessage = null;
				}
				else
				{
					logger.finer("Removing correlation entry " + correlationId + " as timed out");
					removeEntry(correlationId);	
				}
			}
		}
		else
		{
			logger.severe("Correlation " + correlationId + " not found to wait for");
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
					if(inMsg.getType() == Message.MSGTYPE_PROGRESS)
					{
						logger.finer("Received Correlated Progress " + correlationId);
						entry.progressMessage = inMsg;
					}
					else
					{
						logger.finer("Received Correlated Response " + correlationId);
						entry.inboundMessage = inMsg;
						if(entry.correlationListener != null)
						{
							entry.correlationListener.correlatedResponseReceived(entry.outboundMessage, entry.inboundMessage);
							removeEntry(correlationId);
						}
					}
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
