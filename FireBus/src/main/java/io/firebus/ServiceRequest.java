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
		expiry = System.currentTimeMillis() + requestTimeout;
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
		Payload responsePayload = null;
		NodeInformation lastRequestedNode = null;
		while(responsePayload == null  &&  System.currentTimeMillis() < expiry)
		{
			NodeInformation ni = FunctionFinder.findFunction(nodeCore, serviceName); 
			if(ni != null)
			{
				if(ni == lastRequestedNode) 
					try{ Thread.sleep(1000);} catch(Exception e) {}

				logger.finer("Sending service request message to " + ni.getNodeId());
				Message reqMsg = new Message(ni.getNodeId(), nodeCore.getNodeId(), Message.MSGTYPE_REQUESTSERVICE, serviceName, requestPayload);
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
							ni.getFunctionInformation(serviceName).reduceRating();
							lastRequestedNode = ni;
							break;
						} 
						else if(respMsg.getType() == Message.MSGTYPE_SERVICERESPONSE)
						{
							responsePayload = respMsg.getPayload();
							break;
						}
						else if(respMsg.getType() == Message.MSGTYPE_PROGRESS)
						{
							respMsg = nodeCore.getCorrelationManager().waitForResponse(correlation, requestTimeout);
						}
						
						if(System.currentTimeMillis() > expiry)
						{
							String str = "Service request " + serviceName + " has timed out while executing (corr: " + reqMsg.getCorrelation() + ")"; 
							logger.fine(str);
							throw new FunctionTimeoutException(str);
						}
					}
				}
				else
				{
					logger.fine("Service " + serviceName + " on node " + ni.getNodeId() + " has not responded to a service request (corr: " + reqMsg.getCorrelation() + ")");
					ni.getFunctionInformation(serviceName).reduceRating();
					lastRequestedNode = ni;
					//nodeCore.getDirectory().deleteNode(ni);
				}
				nodeCore.getCorrelationManager().removeEntry(correlation);
			}			
		}
		
		if(responsePayload != null)
			return responsePayload;
		else
			throw new FunctionTimeoutException("Service " + serviceName + " could not be found");
	}
	
}
