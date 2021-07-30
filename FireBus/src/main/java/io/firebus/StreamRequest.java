package io.firebus;

import java.util.logging.Logger;

import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.information.FunctionInformation;
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
	protected String requestorFunctionName;
	protected String errorMessage;
	protected FunctionInformation functionInformation;

	public StreamRequest(NodeCore nc, String sn, Payload p, int t)
	{
		nodeCore = nc;
		streamName = sn;
		requestPayload = p;
		errorMessage = null;
		subTimeout = t;
		expiry = System.currentTimeMillis() + subTimeout;
	}
	
	public void initiate(StreamRequestor r, String rfn)
	{
		requestor = r;
		requestorFunctionName = rfn;
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
		FunctionInformation lastRequestedFunction = null;
		FunctionFinder functionFinder = new FunctionFinder(nodeCore, streamName);
		while(streamEndpoint == null  &&  System.currentTimeMillis() < expiry)
		{
			functionInformation = functionFinder.findNext(); 
			if(functionInformation != null)
			{
				if(functionInformation == lastRequestedFunction) 
					try{ Thread.sleep(1000);} catch(Exception e) {}

				lastRequestedFunction = functionInformation;
				logger.finer("Sending stream request message to " + functionInformation.getNodeId());
				Message reqMsg = new Message(functionInformation.getNodeId(), nodeCore.getNodeId(), Message.MSGTYPE_REQUESTSTREAM, streamName, requestPayload);
				int correlation = nodeCore.getCorrelationManager().send(reqMsg, subTimeout);
				Message respMsg = nodeCore.getCorrelationManager().waitForResponse(correlation, subTimeout);
				if(respMsg != null)
				{
					while(System.currentTimeMillis() < expiry)
					{
						if(respMsg.getType() == Message.MSGTYPE_STREAMERROR)
						{
							errorMessage = respMsg.getPayload().getString();
							String errorCodeStr = respMsg.getPayload().metadata.get("errorcode");
							int errorCode = errorCodeStr != null ? Integer.parseInt(errorCodeStr) : 0;
							functionInformation.returnedError();
							throw new FunctionErrorException(errorMessage, errorCode);
						}
						else if(respMsg.getType() == Message.MSGTYPE_FUNCTIONUNAVAILABLE)
						{
							logger.fine("Stream " + streamName + " on node " + functionInformation.getNodeId() + " has responded as unavailable");
							functionInformation.wasUnavailable();
							break;
						} 
						else if(respMsg.getType() == Message.MSGTYPE_STREAMACCEPT)
						{
							Payload acceptPayload = respMsg.getPayload();
							int remoteCorrelation = Integer.parseInt(acceptPayload.metadata.get("correlationid"));
							int idleTimeout = Integer.parseInt(acceptPayload.metadata.get("timeout"));
							streamEndpoint = new StreamEndpoint(nodeCore, streamName, correlation, remoteCorrelation, 0, functionInformation.getNodeId());
							streamEndpoint.setAcceptPayload(acceptPayload);
							streamEndpoint.setRequestPayload(requestPayload);
							nodeCore.getCorrelationManager().setListenerOnEntry(correlation, streamEndpoint, requestorFunctionName, idleTimeout);
							functionInformation.wasSuccesful();
							break;
						}
						
						if(System.currentTimeMillis() > expiry)
						{
							String str = "Stream request " + streamName + " has timed out while executing (corr: " + reqMsg.getCorrelation() + ")"; 
							logger.fine(str);
							functionInformation.timedOutWhileExecuting();
							throw new FunctionTimeoutException(str);
						}
					}
				}
				else
				{
					logger.fine("Stream " + streamName + " on node " + functionInformation.getNodeId() + " has not responded to a stream request (corr: " + reqMsg.getCorrelation() + ")");
					functionInformation.didNotRespond();
				}
			}			
		}
		
		if(streamEndpoint != null)
			return streamEndpoint;
		else
			throw new FunctionTimeoutException("Stream " + streamName + " could not be found");
	}
	
}
