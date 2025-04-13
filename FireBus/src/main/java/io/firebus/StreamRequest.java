package io.firebus;

import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.information.FunctionInformation;
import io.firebus.interfaces.StreamRequestor;
import io.firebus.logging.Level;
import io.firebus.logging.Logger;

public class StreamRequest 
{
	protected NodeCore nodeCore;
	protected String streamName;
	protected Payload requestPayload;
	protected int subTimeout;
	protected long expiry;
	protected StreamRequestor requestor;
	protected String requestorFunctionName;
	protected String errorMessage;
	protected FunctionInformation functionInformation;

	public StreamRequest(NodeCore nc, String sn, Payload p, String rfn, int t)
	{
		nodeCore = nc;
		streamName = sn;
		requestPayload = p;
		errorMessage = null;
		requestorFunctionName = rfn;
		subTimeout = t;
		expiry = System.currentTimeMillis() + subTimeout;
	}
	
	public StreamEndpoint initiate() throws FunctionErrorException, FunctionTimeoutException
	{
		if(Logger.isLevel(Level.FINE)) Logger.fine("fb.stream.request.start", new DataMap("name", streamName));
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
				if(Logger.isLevel(Level.FINER)) Logger.finer("fb.stream.request.send", new DataMap("node", functionInformation.getNodeId()));
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
							if(Logger.isLevel(Level.FINER)) Logger.finer("fb.stream.request.unavailable", new DataMap("stream", streamName, "node", functionInformation.getNodeId()));
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
							nodeCore.getCorrelationManager().setListenerOnEntry(correlation, streamEndpoint, requestorFunctionName, nodeCore.getStreamThreads(), idleTimeout);
							streamEndpoint.activate();
							functionInformation.wasSuccesful();
							break;
						}
						
						if(System.currentTimeMillis() > expiry)
						{
							if(Logger.isLevel(Level.FINER)) Logger.finer("fb.stream.request.timeout", new DataMap("stream", streamName, "node", functionInformation.getNodeId(), "corr", reqMsg.getCorrelation()));
							functionInformation.timedOutWhileExecuting();
							throw new FunctionTimeoutException("Stream request " + streamName + " has timed out while executing (corr: " + reqMsg.getCorrelation() + ")");
						}
					}
				}
				else
				{
					if(Logger.isLevel(Level.FINER)) Logger.finer("fb.stream.request.noresp", new DataMap("stream", streamName, "node", functionInformation.getNodeId(), "corr", reqMsg.getCorrelation()));
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
