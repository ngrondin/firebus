package io.firebus;

import io.firebus.interfaces.CorrelationListener;

public class CorrelationEntry {
	protected int sequence;
	protected Message outboundMessage;
	protected MessageQueue inboundMessages;
	protected NodeCore nodeCore;
	protected CorrelationListener correlationListener;
	protected String listenerFunctionName;
	protected long timeout;
	protected long expiry;
	
	public CorrelationEntry(NodeCore nc, long to)
	{
		nodeCore = nc;
		timeout = to;
		expiry = System.currentTimeMillis() + to;
		sequence = 0;
		inboundMessages = new MessageQueue(100);
	}
	
	public void setListener(CorrelationListener cl, String fn, long to)
	{
		timeout = to;
		expiry = System.currentTimeMillis() + timeout;
		correlationListener = cl;
		listenerFunctionName = fn;
		drainInboundQueue();
	}
	
	public Message popNext() 
	{
		Message next = null;
		int len = inboundMessages.getMessageCount();
		for(int i = 0; i < len; i++) {
			Message msg = inboundMessages.pop();
			if(msg.getCorrelationSequence() == sequence) {
				next = msg;
				sequence++;
				break;
			} else {
				inboundMessages.push(msg);
			}
		}
		return next;
	}

	public void push(Message msg) {
		inboundMessages.push(msg);
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
	

}
