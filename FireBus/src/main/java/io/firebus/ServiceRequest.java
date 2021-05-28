package io.firebus;

import java.util.logging.Logger;

import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.information.FunctionInformation;
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
	protected NodeInformation nodeInformation;

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
		logger.finer("Requesting Service");
		boolean responseReceived = false;
		Payload responsePayload = null;
		NodeInformation lastRequestedNode = null;
		while(responseReceived == false  &&  System.currentTimeMillis() < expiry)
		{
			nodeInformation = FunctionFinder.findFunction(nodeCore, serviceName); 
			if(nodeInformation != null)
			{
				if(nodeInformation == lastRequestedNode) 
					try{ Thread.sleep(1000);} catch(Exception e) {}

				lastRequestedNode = nodeInformation;
				logger.finer("Sending service request message to " + nodeInformation.getNodeId());
				int msgType = requestTimeout >= 0 ? Message.MSGTYPE_REQUESTSERVICE : Message.MSGTYPE_REQUESTSERVICEANDFORGET;
				Message reqMsg = new Message(nodeInformation.getNodeId(), nodeCore.getNodeId(), msgType, serviceName, requestPayload);
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
							logger.fine("Service " + serviceName + " on node " + nodeInformation.getNodeId() + " has responded as unavailable");
							reduceRatingOfServiceForNode(1);							
							break;
						} 
						else if(respMsg.getType() == Message.MSGTYPE_SERVICERESPONSE)
						{
							responseReceived = true;
							responsePayload = respMsg.getPayload();
							resetRatingOfServiceForNode();
							break;
						}
						else if(respMsg.getType() == Message.MSGTYPE_PROGRESS)
						{
							if(msgType == Message.MSGTYPE_REQUESTSERVICEANDFORGET)
							{
								responseReceived = true;
								resetRatingOfServiceForNode();
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
							reduceRatingOfServiceForNode(1);
							throw new FunctionTimeoutException(str);
						}
					}
				}
				else
				{
					logger.fine("Service " + serviceName + " on node " + nodeInformation.getNodeId() + " has not responded to a service request (corr: " + reqMsg.getCorrelation() + ")");
					reduceRatingOfServiceForNode(3);
				}
				nodeCore.getCorrelationManager().removeEntry(correlation);
			}			
		}
		
		if(responseReceived)
			return responsePayload;
		else
			throw new FunctionTimeoutException("Service " + serviceName + " could not be called succesfully");
	}
	
	private void reduceRatingOfServiceForNode(int q) {
		FunctionInformation fi = nodeInformation.getFunctionInformation(serviceName);
		if(fi != null)
			fi.reduceRating(q);		
	}
	
	private void resetRatingOfServiceForNode() {
		FunctionInformation fi = nodeInformation.getFunctionInformation(serviceName);
		if(fi != null)
			fi.resetRating();	
	}
	
	
}
