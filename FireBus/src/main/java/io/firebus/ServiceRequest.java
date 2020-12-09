package io.firebus;

import java.util.logging.Logger;

import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.information.NodeInformation;
import io.firebus.interfaces.ServiceRequestor;

public class ServiceRequest extends Thread
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
	
	public void execute(ServiceRequestor r)
	{
		requestor = r;
		start();
	}
	
	public void run()
	{
		setName("fbServiceReq" + getId());
		try
		{
			Payload responsePayload = execute();
			requestor.requestCallback(responsePayload);
		}
		catch(FunctionErrorException e)
		{
			requestor.requestErrorCallback(e);
		}
		catch(FunctionTimeoutException e)
		{
			requestor.requestTimeout();
		}
	}
	
	public Payload execute() throws FunctionErrorException, FunctionTimeoutException
	{
		logger.finer("Requesting Service");
		boolean responseReceived = false;
		Payload responsePayload = null;
		NodeInformation lastRequestedNode = null;
		while(responseReceived == false  &&  System.currentTimeMillis() < expiry)
		{
			NodeInformation ni = FunctionFinder.findFunction(nodeCore, serviceName); 
			if(ni != null)
			{
				if(ni == lastRequestedNode) 
					try{ Thread.sleep(1000);} catch(Exception e) {}

				logger.finer("Sending service request message to " + ni.getNodeId());
				int msgType = requestTimeout >= 0 ? Message.MSGTYPE_REQUESTSERVICE : Message.MSGTYPE_REQUESTSERVICEANDFORGET;
				Message reqMsg = new Message(ni.getNodeId(), nodeCore.getNodeId(), msgType, serviceName, requestPayload);
				int correlation = nodeCore.getCorrelationManager().send(reqMsg, subTimeout);
				Message respMsg = nodeCore.getCorrelationManager().waitForResponse(correlation, subTimeout);
				if(respMsg != null)
				{
					while(System.currentTimeMillis() < expiry)
					{
						if(respMsg.getType() == Message.MSGTYPE_SERVICEERROR)
						{
							errorMessage = respMsg.getPayload().getString();
							throw new FunctionErrorException(errorMessage);
						}
						else if(respMsg.getType() == Message.MSGTYPE_FUNCTIONUNAVAILABLE)
						{
							logger.fine("Service " + serviceName + " on node " + ni.getNodeId() + " has responded as unavailable");
							ni.getFunctionInformation(serviceName).reduceRating(1);
							lastRequestedNode = ni;
							break;
						} 
						else if(respMsg.getType() == Message.MSGTYPE_SERVICERESPONSE)
						{
							responseReceived = true;
							responsePayload = respMsg.getPayload();
							ni.getFunctionInformation(serviceName).resetRating();
							break;
						}
						else if(respMsg.getType() == Message.MSGTYPE_PROGRESS)
						{
							if(msgType == Message.MSGTYPE_REQUESTSERVICEANDFORGET)
							{
								responseReceived = true;
								ni.getFunctionInformation(serviceName).resetRating();
								break;
							}
							else 
							{
								respMsg = nodeCore.getCorrelationManager().waitForResponse(correlation, requestTimeout);
							}
						}
						
						if(System.currentTimeMillis() > expiry)
						{
							String str = "Service request " + serviceName + " has timed out while executing (corr: " + reqMsg.getCorrelation() + ")"; 
							logger.fine(str);
							ni.getFunctionInformation(serviceName).reduceRating(1);
							throw new FunctionTimeoutException(str);
						}
					}
				}
				else
				{
					logger.fine("Service " + serviceName + " on node " + ni.getNodeId() + " has not responded to a service request (corr: " + reqMsg.getCorrelation() + ")");
					ni.getFunctionInformation(serviceName).reduceRating(3);
					lastRequestedNode = ni;
				}
				nodeCore.getCorrelationManager().removeEntry(correlation);
			}			
		}
		
		if(responseReceived)
			return responsePayload;
		else
			throw new FunctionTimeoutException("Service " + serviceName + " could not be found");
	}
	
}
