package io.firebus;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
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
			NodeInformation ni = FunctionFinder.findFunction(nodeCore, streamName); 
			if(ni != null)
			{
				if(ni == lastRequestedNode) 
					try{ Thread.sleep(1000);} catch(Exception e) {}

				logger.finer("Sending stream request message to " + ni.getNodeId());
				Message reqMsg = new Message(ni.getNodeId(), nodeCore.getNodeId(), Message.MSGTYPE_REQUESTSTREAM, streamName, requestPayload);
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
							logger.fine("Stream " + streamName + " on node " + ni.getNodeId() + " has responded as unavailable");
							ni.getFunctionInformation(streamName).reduceRating(1);
							lastRequestedNode = ni;
							break;
						} 
						else if(respMsg.getType() == Message.MSGTYPE_STREAMACCEPT)
						{
							ByteBuffer bb = ByteBuffer.wrap(respMsg.getPayload().getBytes());
							int remoteCorrelation = bb.getInt();
							long idleTimeout = bb.getLong();
							streamEndpoint = new StreamEndpoint(nodeCore, streamName, correlation, remoteCorrelation, 0, ni.getNodeId());
							nodeCore.getCorrelationManager().setListenerOnEntry(correlation, streamEndpoint, idleTimeout);
							ni.getFunctionInformation(streamName).resetRating();
							break;
						}
						
						if(System.currentTimeMillis() > expiry)
						{
							String str = "Stream request " + streamName + " has timed out while executing (corr: " + reqMsg.getCorrelation() + ")"; 
							logger.fine(str);
							ni.getFunctionInformation(streamName).reduceRating(1);
							throw new FunctionTimeoutException(str);
						}
					}
				}
				else
				{
					logger.fine("Stream " + streamName + " on node " + ni.getNodeId() + " has not responded to a stream request (corr: " + reqMsg.getCorrelation() + ")");
					ni.getFunctionInformation(streamName).reduceRating(3);
					lastRequestedNode = ni;
				}
			}			
		}
		
		if(streamEndpoint != null)
			return streamEndpoint;
		else
			throw new FunctionTimeoutException("Stream " + streamName + " could not be found");
	}
}
