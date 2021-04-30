package io.firebus;

import java.util.logging.Logger;

import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.information.FunctionInformation;
import io.firebus.information.NodeInformation;
import io.firebus.interfaces.CorrelationListener;
import io.firebus.interfaces.ServiceRequestor;

public class ServiceRequestAsync implements CorrelationListener {
	private Logger logger = Logger.getLogger("io.firebus");
	protected NodeCore nodeCore;
	protected String serviceName;
	protected Payload requestPayload;
	protected ServiceRequestor requestor;
	protected int subTimeout;
	protected int requestTimeout;
	protected long expiry;
	protected String errorMessage;
	protected NodeInformation nodeInformation;
	
	
	public ServiceRequestAsync(NodeCore nc, String sn, Payload p, ServiceRequestor r, int t)
	{
		nodeCore = nc;
		serviceName = sn;
		requestPayload = p;
		requestor = r;
		requestTimeout = t;
		subTimeout = 500;
		errorMessage = null;
		expiry = System.currentTimeMillis() + (requestTimeout > -1 ? requestTimeout : subTimeout);
	}
	
	public void execute()  throws FunctionErrorException, FunctionTimeoutException {
		logger.finer("Requesting Service");
		boolean requestInProgress = false;
		NodeInformation lastRequestedNode = null;
		long subExpiry = System.currentTimeMillis() + subTimeout;
		while(requestInProgress == false  &&  System.currentTimeMillis() < subExpiry)
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
					if(respMsg.getType() == Message.MSGTYPE_PROGRESS)
					{
						requestInProgress = true;
						nodeCore.getCorrelationManager().setListenerOnEntry(correlation, this, requestTimeout);
					}
					else //Will always only be Function Unavailable 
					{
						logger.fine("Service " + serviceName + " on node " + nodeInformation.getNodeId() + " has responded as unavailable");
						reduceRatingOfServiceForNode(1);
						nodeCore.getCorrelationManager().removeEntry(correlation);
					}
				}
				else
				{
					logger.fine("Service " + serviceName + " on node " + nodeInformation.getNodeId() + " has not responded to a service request (corr: " + reqMsg.getCorrelation() + ")");
					reduceRatingOfServiceForNode(3);
					nodeCore.getCorrelationManager().removeEntry(correlation);
				}
				
			}			
		}
		
		if(!requestInProgress)
			throw new FunctionTimeoutException("Service " + serviceName + " could not be found");		
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

	public void correlatedResponseReceived(Message outMsg, Message inMsg) {
		if(inMsg.getType() == Message.MSGTYPE_SERVICEERROR)
		{
			errorMessage = inMsg.getPayload().getString();
			requestor.error(new FunctionErrorException(errorMessage));
		}
		else if(inMsg.getType() == Message.MSGTYPE_SERVICERESPONSE)
		{
			Payload responsePayload = inMsg.getPayload();
			resetRatingOfServiceForNode();
			requestor.response(responsePayload);
		}
		nodeCore.getCorrelationManager().removeEntry(inMsg.getCorrelation());		
	}

	public void correlationTimedout(Message outMsg) {
		requestor.timeout();
	}
}
