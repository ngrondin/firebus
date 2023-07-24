package io.firebus;

import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.information.FunctionInformation;
import io.firebus.interfaces.CorrelationListener;
import io.firebus.interfaces.ServiceRequestor;
import io.firebus.logging.Logger;

public class ServiceRequestAsync implements CorrelationListener {
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
	}
	
	public void execute()  throws FunctionErrorException, FunctionTimeoutException {
		Logger.fine("fb.service.requestasync.start", new DataMap("name", serviceName));
		boolean requestInProgress = false;
		long now = System.currentTimeMillis();
		expiry = now + (requestTimeout > -1 ? requestTimeout : subTimeout);
		FunctionInformation lastRequestedFunction = null;
		FunctionFinder functionFinder = new FunctionFinder(nodeCore, serviceName);
		while(requestInProgress == false  &&  System.currentTimeMillis() < expiry)
		{
			functionInformation = functionFinder.findNext(); 
			if(functionInformation != null)
			{
				if(functionInformation == lastRequestedFunction) 
					try{ Thread.sleep(1000);} catch(Exception e) {}
				
				lastRequestedFunction = functionInformation;
				Logger.finer("fb.service.requestasync.send", new DataMap("node", functionInformation.getNodeId()));
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
						nodeCore.getCorrelationManager().setListenerOnEntry(correlation, this, requestorFunctionName, nodeCore.getServiceThreads(), requestTimeout);
					}
					else //Will always only be Function Unavailable 
					{
						Logger.finer("fb.service.requestasync.unavailable", new DataMap("service", serviceName, "node", functionInformation.getNodeId()));
						functionInformation.wasUnavailable();
						nodeCore.getCorrelationManager().removeEntry(correlation);
					}
				}
				else
				{
					Logger.finer("fb.service.requestasync.noresp", new DataMap("service", serviceName, "node", functionInformation.getNodeId(), "corr", reqMsg.getCorrelation()));
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
