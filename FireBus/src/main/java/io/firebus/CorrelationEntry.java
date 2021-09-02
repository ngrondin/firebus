package io.firebus;

import java.util.HashMap;
import java.util.Map;

import io.firebus.interfaces.CorrelationListener;
import io.firebus.utils.DataMap;

public class CorrelationEntry {
	protected int sequence;
	protected Message outboundMessage;
	protected Map<Integer, Message> inboundMessages;
	protected NodeCore nodeCore;
	protected CorrelationListener correlationListener;
	protected String listenerFunctionName;
	protected long start;
	protected long timeout;
	protected long expiry;
	
	public CorrelationEntry(NodeCore nc, long to)
	{
		nodeCore = nc;
		timeout = to;
		start = System.currentTimeMillis();
		expiry = start + to;
		sequence = 0;
		inboundMessages = new HashMap<Integer, Message>();
	}
	
	public void setListener(CorrelationListener cl, String fn, long to)
	{
		timeout = to;
		start = System.currentTimeMillis();
		expiry = start + to;
		correlationListener = cl;
		listenerFunctionName = fn;
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

	public synchronized void push(Message msg) {
		inboundMessages.put(msg.getCorrelationSequence(), msg);
		expiry = System.currentTimeMillis() + timeout;
		drainInboundQueue();
	}
	
	
	public void drainInboundQueue() 
	{
		if(correlationListener != null) {
			Message next = null;
			while((next = popNext()) != null) {
				final Message inboundMessage = next;
				nodeCore.getExecutionThreads().enqueue(new Runnable() {
					public void run() {
						correlationListener.correlatedResponseReceived(outboundMessage, inboundMessage);
					}
				}, listenerFunctionName, -1);
			}
		}
	}
	
	public void expire()
	{
		if(correlationListener != null) {
			nodeCore.getExecutionThreads().enqueue(new Runnable() {
				public void run() {
					correlationListener.correlationTimedout(outboundMessage);
				}
			}, listenerFunctionName, -1);
		}
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
