package io.firebus;

import java.util.logging.Logger;

import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.information.FunctionInformation;
import io.firebus.information.NodeInformation;
import io.firebus.interfaces.StreamRequestor;

public class StreamRequest extends Thread
{
	private Logger logger = Logger.getLogger("io.firebus");
	protected NodeCore nodeCore;
	protected String streamName;
	protected Payload requestPayload;
	protected int subTimeout;
	protected long expiry;
	protected StreamRequestor requestor;
	protected String errorMessage;
	protected NodeInformation nodeInformation;

	public StreamRequest(NodeCore nc, String sn, Payload p, int t)
	{
		nodeCore = nc;
		streamName = sn;
		requestPayload = p;
		errorMessage = null;
		subTimeout = t;
		expiry = System.currentTimeMillis() + subTimeout;
	}
	
	public void initiate(StreamRequestor r)
	{
		requestor = r;
		start();
	}
	
	public void run()
	{
		setName("fbStreamReq" + getId());
		try
		{
			StreamEndpoint endPoint = initiate();
			requestor.initiateCallback(endPoint);
		}
		catch(FunctionErrorException e)
		{
			requestor.initiateErrorCallback(e);
		}
		catch(FunctionTimeoutException e)
		{
			requestor.initiateTimeout();
		}
	}
	
	public StreamEndpoint initiate() throws FunctionErrorException, FunctionTimeoutException
	{
		logger.finer("Requesting Stream");
		StreamEndpoint streamEndpoint = null;
		NodeInformation lastRequestedNode = null;
		while(streamEndpoint == null  &&  System.currentTimeMillis() < expiry)
		{
			nodeInformation = FunctionFinder.findFunction(nodeCore, streamName); 
			if(nodeInformation != null)
			{
				if(nodeInformation == lastRequestedNode) 
					try{ Thread.sleep(1000);} catch(Exception e) {}

				lastRequestedNode = nodeInformation;
				logger.finer("Sending stream request message to " + nodeInformation.getNodeId());
				Message reqMsg = new Message(nodeInformation.getNodeId(), nodeCore.getNodeId(), Message.MSGTYPE_REQUESTSTREAM, streamName, requestPayload);
				int correlation = nodeCore.getCorrelationManager().send(reqMsg, subTimeout);
				Message respMsg = nodeCore.getCorrelationManager().waitForResponse(correlation, subTimeout);
				if(respMsg != null)
				{
					while(System.currentTimeMillis() < expiry)
					{
						if(respMsg.getType() == Message.MSGTYPE_STREAMERROR)
						{
							errorMessage = respMsg.getPayload().getString();
							throw new FunctionErrorException(errorMessage);
						}
						else if(respMsg.getType() == Message.MSGTYPE_FUNCTIONUNAVAILABLE)
						{
							logger.fine("Stream " + streamName + " on node " + nodeInformation.getNodeId() + " has responded as unavailable");
							reduceRatingOfServiceForNode(1);
							break;
						} 
						else if(respMsg.getType() == Message.MSGTYPE_STREAMACCEPT)
						{
							Payload acceptPayload = respMsg.getPayload();
							int remoteCorrelation = Integer.parseInt(acceptPayload.metadata.get("correlationid"));
							int idleTimeout = Integer.parseInt(acceptPayload.metadata.get("timeout"));
							streamEndpoint = new StreamEndpoint(nodeCore, streamName, correlation, remoteCorrelation, 0, nodeInformation.getNodeId());
							streamEndpoint.setAcceptPayload(acceptPayload);
							streamEndpoint.setRequestPayload(requestPayload);
							nodeCore.getCorrelationManager().setListenerOnEntry(correlation, streamEndpoint, idleTimeout);
							resetRatingOfServiceForNode();
							break;
						}
						
						if(System.currentTimeMillis() > expiry)
						{
							String str = "Stream request " + streamName + " has timed out while executing (corr: " + reqMsg.getCorrelation() + ")"; 
							logger.fine(str);
							reduceRatingOfServiceForNode(1);
							throw new FunctionTimeoutException(str);
						}
					}
				}
				else
				{
					logger.fine("Stream " + streamName + " on node " + nodeInformation.getNodeId() + " has not responded to a stream request (corr: " + reqMsg.getCorrelation() + ")");
					reduceRatingOfServiceForNode(3);			
				}
			}			
		}
		
		if(streamEndpoint != null)
			return streamEndpoint;
		else
			throw new FunctionTimeoutException("Stream " + streamName + " could not be found");
	}
	
	private void reduceRatingOfServiceForNode(int q) {
		FunctionInformation fi = nodeInformation.getFunctionInformation(streamName);
		if(fi != null)
			fi.reduceRating(q);		
	}
	
	private void resetRatingOfServiceForNode() {
		FunctionInformation fi = nodeInformation.getFunctionInformation(streamName);
		if(fi != null)
			fi.resetRating();	
	}	
}
