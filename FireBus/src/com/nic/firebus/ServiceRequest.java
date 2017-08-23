package com.nic.firebus;

import java.util.logging.Logger;

import com.nic.firebus.information.NodeInformation;
import com.nic.firebus.interfaces.CorrelationListener;
import com.nic.firebus.interfaces.ServiceRequestor;

public class ServiceRequest implements CorrelationListener
{
	private Logger logger = Logger.getLogger(NodeCore.class.getName());
	protected CorrelationManager correlationManager;
	protected Directory directory;
	protected int nodeId;
	protected String serviceName;
	protected byte[] requestPayload;
	protected int timeout;
	protected long expiry;
	protected ServiceRequestor requestor;
	protected byte[] responsePayload;
	
	public ServiceRequest(String n, byte[] p, int t, ServiceRequestor r, CorrelationManager cm, Directory d, int nid)
	{
		serviceName = n;
		requestPayload = p;
		timeout = t;
		requestor = r;
		nodeId = nid;
		correlationManager = cm;
		directory = d;
		start();
	}

	public void correlatedResponseReceived(Message outMsg, Message inMsg) 
	{
		process(inMsg);
	}
	
	public void correlationTimedout(Message outMsg)
	{
		logger.fine("Setting node as unresponsive");
		NodeInformation ni = directory.getNodeById(outMsg.getDestinationId());
		if(ni != null)
			ni.setUnresponsive();
		process(null);
	}

	protected void start()
	{
		logger.fine("Requesting Service");
		expiry = System.currentTimeMillis() + timeout + 2000;
		process(null);
	}
	
	protected void process(Message inMsg)
	{
		if(System.currentTimeMillis() < expiry)
		{
			if(inMsg != null  &&  inMsg.getType() == Message.MSGTYPE_SERVICERESPONSE)
			{
				logger.fine("Returning Service Response");
				if(requestor != null)
					requestor.requestCallback(inMsg.getPayload());
			}
			else if(inMsg != null  &&  inMsg.getType() == Message.MSGTYPE_SERVICEERROR)
			{
				logger.fine("Returning Service Error");
				if(requestor != null)
					requestor.requestErrorCallback(inMsg.getPayload());
			}
			else
			{
				if(inMsg != null  &&  inMsg.getType() == Message.MSGTYPE_SERVICEUNAVAILABLE)
				{
					logger.fine("No Response Received from Service Request");
					//TODO Set the node as temporarily unavailable
				}
				
				NodeInformation ni = directory.findServiceProvider(serviceName);
				if(ni == null)
				{
					logger.fine("Sending Find Service Message");
					Message findMsg = new Message(0, nodeId, Message.MSGTYPE_FINDSERVICE, serviceName, null);
					correlationManager.asynchronousCall(findMsg, this, 2000);
				}
				else
				{
					logger.fine("Sending Request Message");
					Message msg = new Message(ni.getNodeId(), nodeId, Message.MSGTYPE_REQUESTSERVICE, serviceName, requestPayload);
					correlationManager.asynchronousCall(msg, this, timeout);
				}
			}
		}
		else
		{
			logger.fine("Service request has expired without receiving a response");
			if(requestor != null)
				requestor.requestTimeout();
		}
	}

	public byte[] getResponse()
	{
		while(responsePayload == null  &&  System.currentTimeMillis() < expiry)
			try{ Thread.sleep(10); } catch(Exception e) {}
		return responsePayload;
	}

}
