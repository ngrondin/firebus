package io.firebus;

import io.firebus.interfaces.CorrelationListener;
import io.firebus.interfaces.StreamHandler;

public class StreamEndpoint implements CorrelationListener {

	protected StreamHandler streamHandler;
	protected NodeCore nodeCore;
	protected String streamName;
	protected int localCorrelationId;
	protected int remoteCorrelationId;
	protected int otherNodeId;
	
	protected StreamEndpoint(NodeCore nc, String sn, int lc, int rc, int oni)
	{
		nodeCore = nc;
		streamName = sn;
		localCorrelationId = lc;
		remoteCorrelationId = rc;
		otherNodeId = oni;
	}
	
	public void setHandler(StreamHandler sh)
	{
		streamHandler = sh;
	}
	
	public void send(Payload payload)
	{
		Message msg = new Message(otherNodeId, nodeCore.getNodeId(), Message.MSGTYPE_STREAMDATA, streamName, payload);
		msg.setCorrelation(remoteCorrelationId);
		nodeCore.forkThenRoute(msg);
	}
	
	public void close()
	{
		Message msg = new Message(otherNodeId, nodeCore.getNodeId(), Message.MSGTYPE_STREAMEND, streamName, null);
		msg.setCorrelation(remoteCorrelationId);
		nodeCore.forkThenRoute(msg);
		nodeCore.getCorrelationManager().removeEntry(localCorrelationId);
	}

	public void correlatedResponseReceived(Message outMsg, Message inMsg) {
		if(inMsg.getType() == Message.MSGTYPE_STREAMEND) {
			nodeCore.getCorrelationManager().removeEntry(localCorrelationId);
			if(streamHandler != null)
				streamHandler.streamClosed(this);
		} else if(streamHandler != null) {
			streamHandler.receiveStreamData(inMsg.getPayload(), this);
		}
	}

	public void correlationTimedout(Message outMsg) {
		if(streamHandler != null)
			streamHandler.streamClosed(this);
		
	}
	
	public String toString() {
		return otherNodeId + "." + remoteCorrelationId + " -> " + nodeCore.getNodeId() + "." + localCorrelationId;
	}
}
