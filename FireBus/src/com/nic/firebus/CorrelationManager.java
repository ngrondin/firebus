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
		nodeCore.sendMessage(outMsg);
		int time = 0;
		while(time < timeout  &&  e.inboundMessage == null)
		{
			try{Thread.sleep(10);} catch(Exception err){}
			time += 10;
		}
		entries.remove(e);
		return e.inboundMessage;
	}
	
	public void asynchronousCall(Message outMsg, CorrelationListener cl, int timeout)
	{
		int c = getNextCorrelation();
		outMsg.setCorrelation(c);
		CorrelationEntry e = new CorrelationEntry(outMsg, cl, timeout); 
		entries.put(c, e);
		nodeCore.sendMessage(outMsg);
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
		}
	}
}
