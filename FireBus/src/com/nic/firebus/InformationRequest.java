package com.nic.firebus;

import java.util.logging.Logger;

import com.nic.firebus.information.NodeInformation;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.interfaces.CorrelationListener;
import com.nic.firebus.interfaces.InformationRequestor;

public class InformationRequest implements CorrelationListener
{
	private Logger logger = Logger.getLogger("com.nic.firebus");
	protected CorrelationManager correlationManager;
	protected Directory directory;
	protected int nodeId;
	protected String functionName;
	protected int timeout;
	protected long expiry;
	protected InformationRequestor requestor;
	protected ServiceInformation serviceInformation;
	
	public InformationRequest(String n, int t, InformationRequestor r, CorrelationManager cm, Directory d, int nid)
	{
		functionName = n;
		timeout = t;
		requestor = r;
		correlationManager = cm;
		directory = d;
		nodeId = nid;
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
		logger.info("Getting Service Information");
		expiry = System.currentTimeMillis() + timeout + 2000;
		process(null);
	}
	
	protected void process(Message inMsg)
	{
		if(System.currentTimeMillis() > expiry)
		{
			logger.fine("Information request has expired without receiving a response");
			if(requestor != null)
				requestor.informationRequestTimeout();
		}
		else
		{
			NodeInformation ni = directory.findServiceProvider(functionName);
			if(ni == null)
			{
				logger.fine("Broadcasting Service Information Request Message");
				Message findMsg = new Message(0, nodeId, Message.MSGTYPE_GETFUNCTIONINFORMATION, functionName, null);
				correlationManager.asynchronousCall(findMsg, this, 2000);
			}
			else
			{
				ServiceInformation si = ni.getServiceInformation(functionName);
				if(!si.hasFullInformation())
				{
					logger.fine("Sending Service Information Request Message");
					Message msg = new Message(ni.getNodeId(), nodeId, Message.MSGTYPE_GETFUNCTIONINFORMATION, functionName, null);
					correlationManager.asynchronousCall(msg, this, 2000);
				}
				else
				{
					serviceInformation = si;
					if(requestor != null)
						requestor.informationRequestCallback(serviceInformation);
				}
			}
		}
	}

	public ServiceInformation waitForResponse()
	{
		while(serviceInformation == null  &&  System.currentTimeMillis() < expiry)
			try{ Thread.sleep(10); } catch(Exception e) {}
		return serviceInformation;
	}

}
