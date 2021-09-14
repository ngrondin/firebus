package io.firebus;

import java.util.logging.Logger;

import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.information.FunctionInformation;
import io.firebus.interfaces.CorrelationListener;
import io.firebus.interfaces.ServiceRequestor;

public class ServiceRequestAsync implements CorrelationListener {
	private Logger logger = Logger.getLogger("io.firebus");
	protected NodeCore nodeCore;
	protected String serviceName;
	protected Payload requestPayload;
	protected ServiceRequestor requestor;
	protected String requestorFunctionName;
	protected int subTimeout;
	protected int requestTimeout;
	protected long expiry;
	protected String errorMessage;
	protected FunctionInformation functionInformation;
	
	
	public ServiceRequestAsync(NodeCore nc, String sn, Payload p, ServiceRequestor r, String rfn, int t)
	{
		nodeCore = nc;
		serviceName = sn;
		requestPayload = p;
		requestor = r;
		requestorFunctionName = rfn;
		requestTimeout = t;
		subTimeout = 500;
		errorMessage = null;
		expiry = System.currentTimeMillis() + (requestTimeout > -1 ? requestTimeout : subTimeout);
	}
	
	public void execute()  throws FunctionErrorException, FunctionTimeoutException {
		logger.fine("Requesting Service " + serviceName);
		boolean requestInProgress = false;
		long subExpiry = System.currentTimeMillis() + subTimeout;
		FunctionInformation lastRequestedFunction = null;
		FunctionFinder functionFinder = new FunctionFinder(nodeCore, serviceName);
		while(requestInProgress == false  &&  System.currentTimeMillis() < subExpiry)
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
					if(respMsg.getType() == Message.MSGTYPE_PROGRESS)
					{
						requestInProgress = true;
						functionInformation.returnedProgress();
						nodeCore.getCorrelationManager().setListenerOnEntry(correlation, this, requestorFunctionName, nodeCore.getServiceExecutionThreads(), requestTimeout);
					}
					else //Will always only be Function Unavailable 
					{
						logger.fine("Service " + serviceName + " on node " + functionInformation.getNodeId() + " has responded as unavailable");
						functionInformation.wasUnavailable();
						nodeCore.getCorrelationManager().removeEntry(correlation);
					}
				}
				else
				{
					logger.fine("Service " + serviceName + " on node " + functionInformation.getNodeId() + " has not responded to a service request (corr: " + reqMsg.getCorrelation() + ")");
					functionInformation.didNotRespond();
					nodeCore.getCorrelationManager().removeEntry(correlation);
				}
				
			}			
		}
		
		if(!requestInProgress)
			throw new FunctionTimeoutException("Service " + serviceName + " could not be found");		
	}
	

	public void correlatedResponseReceived(Message outMsg, Message inMsg) {
		if(inMsg.getType() == Message.MSGTYPE_SERVICEERROR)
		{
			errorMessage = inMsg.getPayload().getString();
			String errorCodeStr = inMsg.getPayload().metadata.get("errorcode");
			int errorCode = errorCodeStr != null ? Integer.parseInt(errorCodeStr) : 0;
			functionInformation.returnedError();
			requestor.error(new FunctionErrorException(errorMessage, errorCode));
		}
		else if(inMsg.getType() == Message.MSGTYPE_SERVICERESPONSE)
		{
			Payload responsePayload = inMsg.getPayload();
			functionInformation.wasSuccesful();
			requestor.response(responsePayload);
		}
		nodeCore.getCorrelationManager().removeEntry(inMsg.getCorrelation());		
	}

	public void correlationTimedout(Message outMsg) {
		functionInformation.timedOutWhileExecuting();
		requestor.timeout();
	}
}
