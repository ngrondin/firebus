package io.firebus;

import java.util.HashMap;
import java.util.logging.Logger;

import io.firebus.interfaces.CorrelationListener;
import io.firebus.utils.DataMap;
import io.firebus.utils.StackUtils;

public class CorrelationManager extends Thread
{
	private Logger logger = Logger.getLogger("io.firebus");
	protected HashMap<Integer, CorrelationEntry> entries;
	protected NodeCore nodeCore;
	protected boolean quit;
	protected long lastExpiryCheck;
	protected int expiredCount;
	
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
						logger.warning("Correlation " + correlationId + " wait was interrupted");
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
		long now = System.currentTimeMillis();
		Integer[] ids = getEntryKeyArray();
		for(int i = 0; i < ids.length; i++)
		{
			CorrelationEntry entry = getEntry(ids[i]);
			if(entry != null && now > entry.expiry)
			{
				synchronized(entry)
				{
					logger.warning("Correlation " + ids[i] + " has expired after " + (now - entry.start) + "ms (" + (entry.outboundMessage != null ? entry.outboundMessage.getTypeString() + ":" + entry.outboundMessage.subject : "") + (entry.listenerFunctionName != null ? " for " + entry.listenerFunctionName : "") + ") " + now + " " + entry.expiry + " " + entry.start + " " + entry.timeout);
					entry.expire();
					entry.notify();				
					removeEntry(ids[i]);
					expiredCount++;
				}
			}
		}
		lastExpiryCheck = now;
	}
	
	public void run()
	{
		while(!quit)
		{
			try
			{
				checkExpiredCalls();
				Thread.sleep(500);
			}
			catch(Exception e)
			{
				logger.severe(StackUtils.toString(e.getStackTrace()));
			}
		}
	}
	
	public void close()
	{
		quit = true;
	}
	
	public DataMap getStatus()
	{
		DataMap status = new DataMap();
		DataMap entryMap = new DataMap();
		for(Integer i: entries.keySet())
			entryMap.put(String.valueOf(i), entries.get(i).getStatus());
		status.put("entries", entryMap);
		status.put("entryCount", entries.size());
		status.put("expiredCount", expiredCount);
		status.put("lastExpiryCheck", (System.currentTimeMillis() - lastExpiryCheck));
		return status;
	}
}
