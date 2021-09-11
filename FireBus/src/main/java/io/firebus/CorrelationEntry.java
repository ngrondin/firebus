package io.firebus;

import java.util.HashMap;
import java.util.Map;

import io.firebus.interfaces.CorrelationListener;
import io.firebus.threads.ThreadManager;
import io.firebus.threads.ThreadManager;
import io.firebus.data.DataMap;

public class CorrelationEntry {
	protected int id;
	protected int sequence;
	protected Message outboundMessage;
	protected Map<Integer, Message> inboundMessages;
	protected NodeCore nodeCore;
	protected CorrelationListener correlationListener;
	protected String listenerFunctionName;
	protected long start;
	protected long timeout;
	protected long expiry;
	
	public CorrelationEntry(NodeCore nc, int i, long to)
	{
		nodeCore = nc;
		id = i;
		sequence = 0;
		timeout = to;
		start = System.currentTimeMillis();
		expiry = start + to;
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
				int type = inboundMessage.getType();
				ThreadManager threads = null;
				if(type == Message.MSGTYPE_STREAMDATA || type == Message.MSGTYPE_STREAMEND) 
					threads = nodeCore.getStreamExecutionThreads();
				else
					threads = nodeCore.getServiceExecutionThreads();
				threads.enqueue(new Runnable() {
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
			nodeCore.getServiceExecutionThreads().enqueue(new Runnable() {
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
