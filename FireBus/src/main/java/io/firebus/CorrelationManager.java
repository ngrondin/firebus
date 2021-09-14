package io.firebus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import io.firebus.interfaces.CorrelationListener;
import io.firebus.threads.ThreadManager;
import io.firebus.data.DataMap;
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
	
	protected synchronized CorrelationEntry createEntry(long to)
	{
		int c = nextCorrelation;
		CorrelationEntry entry = new CorrelationEntry(nodeCore, c, to); 
		entries.put(c, entry);
		nextCorrelation++;
		return entry;
	}
	
	protected synchronized CorrelationEntry getEntry(int correlationId)
	{
		return entries.get(correlationId);
	}

	protected synchronized void removeEntry(int correlationId)
	{
		entries.remove(correlationId);
	}
	
	protected synchronized List<CorrelationEntry> getEntryList()
	{
		List<CorrelationEntry> list = new ArrayList<CorrelationEntry>();
		for(Integer i: entries.keySet()) 
			list.add(entries.get(i));
		return list;
	}
	
	public void setListenerOnEntry(int correlationId, CorrelationListener cl, String fn, ThreadManager tm, long timeout)
	{
		CorrelationEntry entry = getEntry(correlationId);
		if(entry != null)
		{
			entry.setListener(cl, fn, tm, timeout);
		}
		else
		{
			logger.severe("Correlation " + correlationId + " not found to set listener on");
		}
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
		CorrelationEntry entry = createEntry(timeout);
		entry.outboundMessage = outMsg;
		if(cl != null) {
			ThreadManager tm = outMsg.getType() == Message.MSGTYPE_REQUESTSTREAM ? nodeCore.getStreamExecutionThreads() : nodeCore.getServiceExecutionThreads();
			entry.setListener(cl, null, tm, timeout);
		}
		outMsg.setCorrelation(entry.id, 0);
		nodeCore.enqueue(outMsg);
		return entry.id;
	}
	
	
	
	public void receiveResponse(Message inMsg)
	{
		int correlationId = inMsg.getCorrelation();
		if(correlationId != 0)
		{
			CorrelationEntry entry = getEntry(correlationId);
			if(entry != null)
			{
				entry.push(inMsg);
			}
			else
			{
				if(inMsg.getType() != Message.MSGTYPE_FUNCTIONINFORMATION) 
					logger.warning("No correlation entry found for '" + inMsg.getTypeString() + "' response from service " + inMsg.getSubject() + " (corr: " + correlationId + " " + inMsg.getOriginatorId() + "->" + inMsg.getDestinationId() + ")");
			}
		}
	}
	
	public void run()
	{
		while(!quit)
		{
			try
			{
				long now = System.currentTimeMillis();
				List<CorrelationEntry> list = getEntryList();
				for(CorrelationEntry entry: list) 
				{
					if(entry.checkExipry()) 
					{
						removeEntry(entry.id);
						expiredCount++;
					}
				}
				lastExpiryCheck = now;
				Thread.sleep(100);
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
