package com.nic.firebus;

import java.util.logging.Logger;

import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.NodeInformation;
import com.nic.firebus.interfaces.ServiceRequestor;

public class ServiceRequest extends Thread
{
	private Logger logger = Logger.getLogger("com.nic.firebus");
	protected NodeCore nodeCore;
	protected String serviceName;
	protected Payload requestPayload;
	protected int timeout;
	protected long expiry;
	protected ServiceRequestor requestor;
	protected Payload responsePayload;
	protected String errorMessage;

	public ServiceRequest(NodeCore nc, String sn, Payload p, int t, ServiceRequestor r)
	{
		nodeCore = nc;
		serviceName = sn;
		requestPayload = p;
		timeout = t;
		requestor = r;
		responsePayload = null;
		errorMessage = null;
		expiry = System.currentTimeMillis() + timeout;
		start();
	}
	
	public void run()
	{
		while(responsePayload == null  &&  System.currentTimeMillis() < expiry)
		{
			NodeInformation ni = nodeCore.getDirectory().findServiceProvider(serviceName);
			if(ni == null)
			{
				logger.fine("Broadcasting Service Information Request Message");
				Message findMsg = new Message(0, nodeCore.getNodeId(), Message.MSGTYPE_GETFUNCTIONINFORMATION, serviceName, null);
				Message respMsg = nodeCore.getCorrelationManager().synchronousCall(findMsg, timeout);
				if(respMsg != null)
				{
					ni = nodeCore.getDirectory().getNodeById(respMsg.getOriginatorId());
				}
			}	

			if(ni != null)
			{
				logger.info("Requesting Service");
				Message msg = new Message(ni.getNodeId(), nodeCore.getNodeId(), Message.MSGTYPE_REQUESTSERVICE, serviceName, requestPayload);
				Message resp = nodeCore.getCorrelationManager().synchronousCall(msg, timeout);
				if(resp != null)
				{
					responsePayload = resp.getPayload();
				}
				else
				{
					nodeCore.getDirectory().deleteNode(ni);
				}
			}			
		}
	}
	
	public Payload waitForResponse() throws FunctionErrorException
	{
		while(responsePayload == null  &&  errorMessage == null  &&  System.currentTimeMillis() < expiry)
			try{ Thread.sleep(10); } catch(Exception e) {}
		
		if(responsePayload != null)
				return responsePayload;
		else
			throw new FunctionErrorException(errorMessage);
	}

}
