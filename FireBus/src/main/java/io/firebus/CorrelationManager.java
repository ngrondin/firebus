package io.firebus;

import java.util.HashMap;
import java.util.logging.Logger;

import io.firebus.interfaces.CorrelationListener;
import io.firebus.utils.StackUtils;

public class CorrelationManager extends Thread
{
	private Logger logger = Logger.getLogger("io.firebus");
	protected HashMap<Integer, CorrelationEntry> entries;
	protected NodeCore nodeCore;
	protected boolean quit;
	
	protected int nextCorrelation = 1;
	
	public CorrelationManager(NodeCore nc)
	{
		entries = new HashMap<Integer, CorrelationEntry>();
		nodeCore = nc;
		quit = false;
		setName("fbCorrMgr");
		start();
	}
	
	protected synchronized int createEntry(long to)
	{
		CorrelationEntry entry = new CorrelationEntry(nodeCore, to); 
		int c = nextCorrelation;
		nextCorrelation++;
		while(entries.containsKey(c)) { //This should never happen but I'm trying to uncover up a bug
			logger.severe("Correlation manager tried to create duplicate entry: " + c); 
			nextCorrelation++;
		}
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
	
	public Message setListenerOnEntry(int correlationId, CorrelationListener cl, String fn, long timeout)
	{
		CorrelationEntry entry = getEntry(correlationId);
		Message message = null;
		if(entry != null)
		{
			synchronized(entry)
			{
				entry.setListener(cl, fn, timeout);
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
				while(System.currentTimeMillis() < entry.expiry  &&  (message = entry.popNext()) == null)
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
			}
		}
		else
		{
			logger.severe("Correlation " + correlationId + " not found to wait for\r\n" + StackUtils.getStackString());
		}
		return message;
	}
	
	public Message sendAndWait(Message outMsg, int timeout)
	{
		int c = send(outMsg, null, timeout);
		Message m = waitForResponse(c, timeout);
		removeEntry(c);
		return m;
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
		outMsg.setCorrelation(c, 0);
		nodeCore.enqueue(outMsg);
		return c;
	}
	
	
	
	public void receiveResponse(Message inMsg)
	{
		int correlationId = inMsg.getCorrelation();
		int correlationSequence = inMsg.getCorrelationSequence();
		//System.out.println(correlationId + " " + correlationSequence);
		if(correlationId != 0)
		{
			CorrelationEntry entry = getEntry(correlationId);
			if(entry != null)
			{
				synchronized(entry)
				{
					logger.finer("Received Correlated message " + correlationId + " sequence " + correlationSequence);
					entry.push(inMsg);
					entry.notify();
				}
			}
			else
			{
				if(inMsg.getType() != Message.MSGTYPE_FUNCTIONINFORMATION) 
					logger.warning("No correlation entry found for '" + inMsg.getTypeString() + "' response from service " + inMsg.getSubject() + " (corr: " + correlationId + ")");
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
			if(entry != null && currTime > entry.expiry)
			{
				synchronized(entry)
				{
					logger.warning("Correlation " + ids[i] + " has expired");
					entry.expire();
					entry.notify();				
					removeEntry(ids[i]);
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
