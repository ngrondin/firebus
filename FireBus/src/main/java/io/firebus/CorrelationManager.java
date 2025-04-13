package io.firebus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.firebus.data.DataMap;
import io.firebus.interfaces.CorrelationListener;
import io.firebus.logging.Logger;
import io.firebus.threads.ThreadManager;

public class CorrelationManager extends Thread
{
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
			entry.setListener(cl, fn, tm, timeout);
		else
			Logger.severe("fb.correlation.notfoundtowaitfor", new DataMap("corr", correlationId));
	}


	public Message waitForResponse(int correlationId, int timeout)
	{
		CorrelationEntry entry = getEntry(correlationId);
		Message message = null;
		if(entry != null)
			message = entry.waitForNext(timeout);
		else
			Logger.severe("fb.correlation.notfoundtowaitfor", new DataMap("corr", correlationId));
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
		entry.firstOutboundMessage = outMsg;
		if(cl != null) {
			ThreadManager tm = outMsg.getType() == Message.MSGTYPE_REQUESTSTREAM ? nodeCore.getStreamThreads() : nodeCore.getServiceThreads();
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
					Logger.warning("fb.correlation.notfound", new DataMap("corr", correlationId, "service", inMsg.getSubject(), "msgtype", inMsg.getTypeString(), "originator", inMsg.getOriginatorId(), "destination", inMsg.getDestinationId()));
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
					if(entry.checkExipred()) 
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
				Logger.severe("fb.correlation.clean", "Error while cleaning correlations", e);
			}
		}
	}
	
	public void close()
	{
		quit = true;
	}
	
	public synchronized DataMap getStatus()
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
