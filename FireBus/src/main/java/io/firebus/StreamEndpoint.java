package io.firebus;

import io.firebus.exceptions.FunctionErrorException;
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
	protected Queue<Message> preHandlerInQueue;
	protected Queue<Message> preActiveOutQueue;
	protected Payload requestPayload;
	protected Payload acceptPayload;
	protected boolean active;
	protected boolean closed;
	
	protected StreamEndpoint(NodeCore nc, String sn, int lc, int rc, int rcs, int rni) {
		nodeCore = nc;
		streamName = sn;
		localCorrelationId = lc;
		remoteCorrelationId = rc;
		remoteCorrelationSequence = rcs;
		remoteNodeId = rni;
		preHandlerInQueue = new Queue<Message>(100);
		preActiveOutQueue = new Queue<Message>(100);
		active = false;
		closed = false;
	}
	
	public void setRequestPayload(Payload p) {
		requestPayload = p;
	}
	
	public Payload getRequestPayload() {
		return requestPayload;
	}
	
	public void setAcceptPayload(Payload p) {
		acceptPayload = p;
	}
	
	public Payload getAcceptPayload() {
		return acceptPayload;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public boolean isClosed() {
		return closed;
	}
	
	protected void activate() {
		active = true;
		while(preActiveOutQueue.getDepth() > 0) {
			Message outMsg = preActiveOutQueue.pop();
			_send(outMsg);
		}
	}

	public synchronized void setHandler(StreamHandler sh) {
		streamHandler = sh;
		if(streamHandler != null) {
			while(preHandlerInQueue.getDepth() > 0) {
				Message inMsg = preHandlerInQueue.pop(); 
				_processInbound(inMsg);
			}
		}
	}
		
	private void _send(Message msg) {
		if(active) {
			msg.setCorrelation(remoteCorrelationId, remoteCorrelationSequence);
			remoteCorrelationSequence++;
			nodeCore.enqueue(msg);
			if(msg.getType() == Message.MSGTYPE_STREAMEND || msg.getType() == Message.MSGTYPE_STREAMERROR) //This is because the ep can be closed before activation, and so the closing needs to be deffered to when the message is sent.
				_disconnect();
		} else {
			preActiveOutQueue.push(msg);
		}
	}
	
	public void send(Payload payload) {
		if(!closed) {
			_send(new Message(remoteNodeId, nodeCore.getNodeId(), Message.MSGTYPE_STREAMDATA, streamName, payload));
		}
	}

	public void close() {
		if(!closed) {
			_send(new Message(remoteNodeId, nodeCore.getNodeId(), Message.MSGTYPE_STREAMEND, streamName, null));
		}
	}
	
	public void error(FunctionErrorException error) {
		if(!closed) {
			Payload payload = new Payload(error.getMessage());
			payload.metadata.put("errorcode", String.valueOf(error.getErrorCode()));
			_send(new Message(remoteNodeId, nodeCore.getNodeId(), Message.MSGTYPE_STREAMERROR, streamName, payload));
		}
	}
	
	public synchronized void correlatedResponseReceived(Message outMsg, Message inMsg) {
		if(streamHandler != null) {
			_processInbound(inMsg);
		} else {
			preHandlerInQueue.push(inMsg);
		}
	}
	
	private void _processInbound(Message inMsg) {
		if(streamHandler != null) {
			switch(inMsg.getType()) {
			case Message.MSGTYPE_STREAMDATA:
				streamHandler.receiveStreamData(inMsg.getPayload());
				break;
			case Message.MSGTYPE_STREAMEND:
				_disconnect();
				break;
			case Message.MSGTYPE_STREAMERROR:
				String errorMessage = inMsg.getPayload().getString();
				String errorCodeStr = inMsg.getPayload().metadata.get("errorcode");
				int errorCode = errorCodeStr != null ? Integer.parseInt(errorCodeStr) : 0;
				streamHandler.streamError(new FunctionErrorException(errorMessage, errorCode));
				_disconnect();
				break;	
			}
		}
	}

	public void correlationTimedout(Message outMsg) {
		close();
	}
	
	private void _disconnect() {
		closed = true;
		nodeCore.getCorrelationManager().removeEntry(localCorrelationId);
		if(streamHandler != null)
			streamHandler.streamClosed();		
	}
	
	public String toString() {
		return remoteNodeId + "." + remoteCorrelationId + " -> " + nodeCore.getNodeId() + "." + localCorrelationId;
	}
}
