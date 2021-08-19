package io.firebus;

import java.util.logging.Logger;

import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.information.FunctionInformation;
import io.firebus.interfaces.ServiceRequestor;

public class ServiceRequest
{
	private Logger logger = Logger.getLogger("io.firebus");
	protected NodeCore nodeCore;
	protected String serviceName;
	protected Payload requestPayload;
	protected int subTimeout;
	protected int requestTimeout;
	protected long expiry;
	protected ServiceRequestor requestor;
	protected String errorMessage;
	protected FunctionInformation functionInformation;

	public ServiceRequest(NodeCore nc, String sn, Payload p, int t)
	{
		nodeCore = nc;
		serviceName = sn;
		requestPayload = p;
		requestTimeout = t;
		subTimeout = 500;
		errorMessage = null;
		expiry = System.currentTimeMillis() + (requestTimeout > -1 ? requestTimeout : subTimeout);
	}
	
	public Payload execute() throws FunctionErrorException, FunctionTimeoutException
	{
		logger.fine("Requesting Service " + serviceName);
		boolean responseReceived = false;
		Payload responsePayload = null;
		FunctionInformation lastRequestedFunction = null;
		FunctionFinder functionFinder = new FunctionFinder(nodeCore, serviceName);
		while(responseReceived == false  &&  System.currentTimeMillis() < expiry)
		{
			functionInformation = functionFinder.findNext(); 
			if(functionInformation != null)
			{
				if(functionInformation == lastRequestedFunction) 
					try{ Thread.sleep(1000);} catch(Exception e) {}

				lastRequestedFunction = functionInformation;
				logger.fine("Sending service request message to " + functionInformation.getNodeId());
				int msgType = requestTimeout >= 0 ? Message.MSGTYPE_REQUESTSERVICE : Message.MSGTYPE_REQUESTSERVICEANDFORGET;
				Message reqMsg = new Message(functionInformation.getNodeId(), nodeCore.getNodeId(), msgType, serviceName, requestPayload);
				int correlation = nodeCore.getCorrelationManager().send(reqMsg, subTimeout);
				Message respMsg = nodeCore.getCorrelationManager().waitForResponse(correlation, subTimeout);
				if(respMsg != null)
				{
					while(System.currentTimeMillis() < expiry)
					{
						if(respMsg.getType() == Message.MSGTYPE_SERVICEERROR)
						{
							errorMessage = respMsg.getPayload().getString();
							String errorCodeStr = respMsg.getPayload().metadata.get("errorcode");
							int errorCode = errorCodeStr != null ? Integer.parseInt(errorCodeStr) : 0;
							functionInformation.returnedError();
							nodeCore.getCorrelationManager().removeEntry(correlation);
							throw new FunctionErrorException(errorMessage, errorCode);
						}
						else if(respMsg.getType() == Message.MSGTYPE_FUNCTIONUNAVAILABLE)
						{
							logger.fine("Service " + serviceName + " on node " + functionInformation.getNodeId() + " has responded as unavailable");
							functionInformation.wasUnavailable();
							break;
						} 
						else if(respMsg.getType() == Message.MSGTYPE_SERVICERESPONSE)
						{
							responseReceived = true;
							responsePayload = respMsg.getPayload();
							functionInformation.wasSuccesful();
							break;
						}
						else if(respMsg.getType() == Message.MSGTYPE_PROGRESS)
						{
							if(msgType == Message.MSGTYPE_REQUESTSERVICEANDFORGET)
							{
								responseReceived = true;
								break;
							}
							else 
							{
								functionInformation.returnedProgress();
								respMsg = nodeCore.getCorrelationManager().waitForResponse(correlation, requestTimeout);
							}
						}
						
						if(System.currentTimeMillis() > expiry)
						{
							String str = "Service request " + serviceName + " has timed out while executing (corr: " + reqMsg.getCorrelation() + ")"; 
							logger.fine(str);
							functionInformation.timedOutWhileExecuting();
							throw new FunctionTimeoutException(str);
						}
					}
				}
				else
				{
					logger.fine("Service " + serviceName + " on node " + functionInformation.getNodeId() + " has not responded to a service request (corr: " + reqMsg.getCorrelation() + ")");
					functionInformation.didNotRespond();
				}
				nodeCore.getCorrelationManager().removeEntry(correlation);
			}			
		}
		
		if(responseReceived)
			return responsePayload;
		else
			throw new FunctionTimeoutException("Service " + serviceName + " could not be called succesfully");
	}
	
}
