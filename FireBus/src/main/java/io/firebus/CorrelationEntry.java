package io.firebus;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import io.firebus.interfaces.CorrelationListener;
import io.firebus.threads.ThreadManager;
import io.firebus.data.DataMap;

public class CorrelationEntry {
	private Logger logger = Logger.getLogger("io.firebus");
	protected int id;
	protected int sequence;
	protected Message outboundMessage;
	protected Map<Integer, Message> inboundMessages;
	protected NodeCore nodeCore;
	protected CorrelationListener correlationListener;
	protected String listenerFunctionName;
	protected ThreadManager threadManager;
	protected long start;
	protected long timeout;
	protected long expiry;
	protected boolean expired;
	
	public CorrelationEntry(NodeCore nc, int i, long to)
	{
		nodeCore = nc;
		id = i;
		sequence = 0;
		timeout = to;
		start = System.currentTimeMillis();
		expiry = start + to;
		expired = false;
		inboundMessages = new HashMap<Integer, Message>();
	}
	
	public synchronized void setListener(CorrelationListener cl, String fn, ThreadManager tm, long to)
	{
		timeout = to;
		start = System.currentTimeMillis();
		expiry = start + to;
		correlationListener = cl;
		listenerFunctionName = fn;
		threadManager = tm;
		drainInboundQueue();
	}
	
	public synchronized Message popNext() 
	{
		Message next = inboundMessages.get(sequence);
		if(next != null) {
			inboundMessages.remove(sequence);
			sequence++;
		}
		return next;
	}
	
	public synchronized Message waitForNext(int to)
	{
		Message message = null;
		if(expired == false) {
			timeout = to;
			expiry = System.currentTimeMillis() + timeout;
			try
			{
				while(expired == false  &&  (message = popNext()) == null)
					wait();
			}
			catch(InterruptedException e)
			{
				logger.warning("Correlation " + id + " wait was interrupted");
			}
		}
		return message;
	}

	public synchronized void push(Message msg) {
		int seq = msg.getCorrelationSequence();
		logger.finer("Received Correlated message " + id + " sequence " + seq);		
		inboundMessages.put(seq, msg);
		expiry = System.currentTimeMillis() + timeout;
		drainInboundQueue();
		notifyAll();
	}
	
	
	public void drainInboundQueue() 
	{
		if(correlationListener != null) {
			Message next = null;
			while((next = popNext()) != null) {
				final Message inboundMessage = next;
				threadManager.enqueue(new Runnable() {
					public void run() {
						correlationListener.correlatedResponseReceived(outboundMessage, inboundMessage);
					}
				}, listenerFunctionName, -1);
			}
		}
	}
	
	public synchronized boolean checkExipred() 
	{
		long now = System.currentTimeMillis();
		if(expired == false && now > expiry) 
		{
			logger.warning("Correlation " + id + " has expired after " + (now - start) + "ms (" + (outboundMessage != null ? outboundMessage.getTypeString() + ":" + outboundMessage.subject + ":" + outboundMessage.getOriginatorId() + "->" + outboundMessage.getDestinationId() : "") + (listenerFunctionName != null ? " for " + listenerFunctionName : "") + ") exp:" + expiry + " start:" + start + " timeout:" + timeout);
			expired = true;
			if(correlationListener != null) {
				threadManager.enqueue(new Runnable() {
					public void run() {
						correlationListener.correlationTimedout(outboundMessage);
					}
				}, listenerFunctionName, -1);
			}
			notifyAll();				
		}
		return expired;
	}
	
	public DataMap getStatus()
	{
		DataMap status = new DataMap();
		long now = System.currentTimeMillis();
		status.put("startSince", (now - start));
		status.put("expiryUntil", (expiry - now));
		if(listenerFunctionName != null)
			status.put("listenerFunctionName", listenerFunctionName);
		return status;
	}
}
