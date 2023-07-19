package io.firebus;

import io.firebus.interfaces.CorrelationListener;
import io.firebus.interfaces.StreamHandler;
import io.firebus.utils.Queue;

public class StreamEndpoint implements CorrelationListener {

	protected StreamHandler streamHandler;
	protected NodeCore nodeCore;
	protected String streamName;
	protected int localCorrelationId;
	protected int remoteCorrelationId;
	protected int remoteCorrelationSequence;
	protected int remoteNodeId;
	protected Queue<Message> inQueue;
	protected Queue<Message> preActiveOutQueue;
	protected Payload requestPayload;
	protected Payload acceptPayload;
	protected boolean active;
	protected boolean closed;
	
	protected StreamEndpoint(NodeCore nc, String sn, int lc, int rc, int rcs, int rni)
	{
		nodeCore = nc;
		streamName = sn;
		localCorrelationId = lc;
		remoteCorrelationId = rc;
		remoteCorrelationSequence = rcs;
		remoteNodeId = rni;
		inQueue = new Queue<Message>(100);
		preActiveOutQueue = new Queue<Message>(100);
		active = false;
		closed = false;
	}
	
	public void setRequestPayload(Payload p)
	{
		requestPayload = p;
	}
	
	public Payload getRequestPayload()
	{
		return requestPayload;
	}
	
	public void setAcceptPayload(Payload p)
	{
		acceptPayload = p;
	}
	
	public Payload getAcceptPayload()
	{
		return acceptPayload;
	}
	
	public boolean isActive() 
	{
		return active;
	}
	
	public boolean isClosed() 
	{
		return closed;
	}
	
	protected void activate() 
	{
		if(!closed) {
			active = true;
			while(preActiveOutQueue.getDepth() > 0) {
				Message outMsg = preActiveOutQueue.pop();
				send(outMsg);
			}
		}
	}

	public synchronized void setHandler(StreamHandler sh)
	{
		streamHandler = sh;
		if(streamHandler != null) {
			while(inQueue.getDepth() > 0) {
				Message inMsg = inQueue.pop();
				if(inMsg.getType() == Message.MSGTYPE_STREAMEND) {
					streamHandler.streamClosed(this);
				} else {
					streamHandler.receiveStreamData(inMsg.getPayload(), this);
				}
			}
		}
	}
		
	private void send(Message msg) 
	{
		if(!closed) {
			if(active) {
				msg.setCorrelation(remoteCorrelationId, remoteCorrelationSequence);
				remoteCorrelationSequence++;
				nodeCore.enqueue(msg);
				if(msg.getType() == Message.MSGTYPE_STREAMEND) {
					deactivate();		
				}
			} else {
				preActiveOutQueue.push(msg);
			}
		}
	}
	
	public void send(Payload payload)
	{
		send(new Message(remoteNodeId, nodeCore.getNodeId(), Message.MSGTYPE_STREAMDATA, streamName, payload));
	}

	
	public void close()
	{
		send(new Message(remoteNodeId, nodeCore.getNodeId(), Message.MSGTYPE_STREAMEND, streamName, null));
	}
	
	private void deactivate() 
	{
		active = false;
		closed = true;
		nodeCore.getCorrelationManager().removeEntry(localCorrelationId);
		if(streamHandler != null)
			streamHandler.streamClosed(this);		
	}

	public synchronized void correlatedResponseReceived(Message outMsg, Message inMsg) {
		if(inMsg.getType() == Message.MSGTYPE_STREAMEND) {
			deactivate();
			if(streamHandler == null) 
				inQueue.push(inMsg);
		} else if(streamHandler != null) {
			streamHandler.receiveStreamData(inMsg.getPayload(), this);
		} else {
			inQueue.push(inMsg);
		}
	}

	public void correlationTimedout(Message outMsg) {
		deactivate();
	}
	
	public String toString() {
		return remoteNodeId + "." + remoteCorrelationId + " -> " + nodeCore.getNodeId() + "." + localCorrelationId;
	}
}
