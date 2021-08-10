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
	protected Payload requestPayload;
	protected Payload acceptPayload;
	protected boolean active;
	
	protected StreamEndpoint(NodeCore nc, String sn, int lc, int rc, int rcs, int rni)
	{
		nodeCore = nc;
		streamName = sn;
		localCorrelationId = lc;
		remoteCorrelationId = rc;
		remoteCorrelationSequence = rcs;
		remoteNodeId = rni;
		inQueue = new Queue<Message>(100);
		active = true;
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

	public synchronized void setHandler(StreamHandler sh)
	{
		streamHandler = sh;
		if(streamHandler != null) {
			while(inQueue.getDepth() > 0) {
				Message inMsg = inQueue.pop();
				streamHandler.receiveStreamData(inMsg.getPayload(), this);
			}
		}
	}
	
	public void send(Payload payload)
	{
		if(active) {
			Message msg = new Message(remoteNodeId, nodeCore.getNodeId(), Message.MSGTYPE_STREAMDATA, streamName, payload);
			msg.setCorrelation(remoteCorrelationId, remoteCorrelationSequence);
			remoteCorrelationSequence++;
			nodeCore.enqueue(msg);
		}
	}
	
	public void close()
	{
		if(active) {
			Message msg = new Message(remoteNodeId, nodeCore.getNodeId(), Message.MSGTYPE_STREAMEND, streamName, null);
			msg.setCorrelation(remoteCorrelationId, remoteCorrelationSequence);
			remoteCorrelationSequence++;
			nodeCore.enqueue(msg);
			nodeCore.getCorrelationManager().removeEntry(localCorrelationId);
			active = false;
		}
	}

	public synchronized void correlatedResponseReceived(Message outMsg, Message inMsg) {
		if(inMsg.getType() == Message.MSGTYPE_STREAMEND) {
			nodeCore.getCorrelationManager().removeEntry(localCorrelationId);
			if(streamHandler != null)
				streamHandler.streamClosed(this);
			active = false;
		} else if(streamHandler != null) {
			streamHandler.receiveStreamData(inMsg.getPayload(), this);
		} else {
			inQueue.push(inMsg);
		}
	}

	public void correlationTimedout(Message outMsg) {
		if(streamHandler != null) {
			active = false;
			streamHandler.streamClosed(this);
		}
		
	}
	
	public String toString() {
		return remoteNodeId + "." + remoteCorrelationId + " -> " + nodeCore.getNodeId() + "." + localCorrelationId;
	}
}
