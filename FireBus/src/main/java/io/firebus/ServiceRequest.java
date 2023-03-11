package io.firebus;

import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.information.FunctionInformation;
import io.firebus.interfaces.ServiceRequestor;
import io.firebus.logging.Logger;

public class ServiceRequest
{
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
	}
	
	public Payload execute() throws FunctionErrorException, FunctionTimeoutException
	{
		Logger.fine("fb.service.request.start", new DataMap("name", serviceName));
		boolean responseReceived = false;
		Payload responsePayload = null;
		FunctionInformation lastRequestedFunction = null;
		FunctionFinder functionFinder = new FunctionFinder(nodeCore, serviceName);
		expiry = System.currentTimeMillis() + (requestTimeout > -1 ? requestTimeout : subTimeout);
		while(responseReceived == false  &&  System.currentTimeMillis() < expiry)
		{
			functionInformation = functionFinder.findNext(); 
			if(functionInformation != null)
			{
				if(functionInformation == lastRequestedFunction) 
					try{ Thread.sleep(1000);} catch(Exception e) {}

				lastRequestedFunction = functionInformation;
				Logger.finer("fb.service.request.send", new DataMap("node", functionInformation.getNodeId()));
				int msgType = requestTimeout >= 0 ? Message.MSGTYPE_REQUESTSERVICE : Message.MSGTYPE_REQUESTSERVICEANDFORGET;
				Message reqMsg = new Message(functionInformation.getNodeId(), nodeCore.getNodeId(), msgType, serviceName, requestPayload);
				int correlation = nodeCore.getCorrelationManager().send(reqMsg, subTimeout);
				Message respMsg = nodeCore.getCorrelationManager().waitForResponse(correlation, subTimeout);
				if(respMsg != null)
				{
					while(respMsg != null && System.currentTimeMillis() < expiry)
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
							Logger.finer("fb.service.request.unavailable", new DataMap("service", serviceName, "node", functionInformation.getNodeId()));
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
							Logger.finer("fb.service.request.timeout", new DataMap("service", serviceName, "node", functionInformation.getNodeId(), "corr", reqMsg.getCorrelation()));
							functionInformation.timedOutWhileExecuting();
							throw new FunctionTimeoutException("Service request " + serviceName + " has timed out while executing (corr: " + reqMsg.getCorrelation() + ")");
						}
					}
				}
				else
				{
					Logger.finer("fb.service.request.noresp", new DataMap("service", serviceName, "node", functionInformation.getNodeId(), "corr", reqMsg.getCorrelation()));
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
