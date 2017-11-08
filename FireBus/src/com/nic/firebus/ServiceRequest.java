package com.nic.firebus;

import java.util.logging.Logger;

import com.nic.firebus.distributables.DistributableService;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.NodeInformation;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.interfaces.ServiceRequestor;
import com.nic.firebus.utils.JSONObject;

public class ServiceRequest extends Thread
{
	private Logger logger = Logger.getLogger("com.nic.firebus");
	protected NodeCore nodeCore;
	protected String serviceName;
	protected Payload requestPayload;
	protected int subTimeout;
	protected int totalTimeout;
	protected long expiry;
	protected ServiceRequestor requestor;
	protected String errorMessage;

	public ServiceRequest(NodeCore nc, String sn, Payload p, int t)
	{
		nodeCore = nc;
		serviceName = sn;
		requestPayload = p;
		totalTimeout = t;
		subTimeout = totalTimeout / 4;
		errorMessage = null;
		expiry = System.currentTimeMillis() + totalTimeout;
	}
	
	public void execute(ServiceRequestor r)
	{
		requestor = r;
		start();
	}
	
	public void run()
	{
		try
		{
			Payload responsePayload = execute();
			requestor.requestCallback(responsePayload);
		}
		catch(FunctionErrorException e)
		{
			requestor.requestErrorCallback(e);
		}		
	}
	
	public Payload execute() throws FunctionErrorException
	{
		Payload responsePayload = null;
		while(responsePayload == null  &&  System.currentTimeMillis() < expiry)
		{
			NodeInformation ni = nodeCore.getDirectory().findServiceProvider(serviceName);
			if(ni == null)
			{
				logger.fine("Broadcasting Service Information Request Message");
				Message findMsg = new Message(0, nodeCore.getNodeId(), Message.MSGTYPE_GETFUNCTIONINFORMATION, serviceName, null);
				Message respMsg = nodeCore.getCorrelationManager().synchronousCall(findMsg, totalTimeout);
				if(respMsg != null)
				{
					ni = nodeCore.getDirectory().getNodeById(respMsg.getOriginatorId());
				}
			}
			
			if(ni == null)
			{
				try
				{
					logger.fine("Trying to retreive distributable service");
					ServiceRequest request = new ServiceRequest(nodeCore, "firebus_distributable_services_source", new Payload(serviceName.getBytes()), subTimeout);
					Payload response = request.execute();
					if(response != null)
					{
						logger.fine("Instantiating distributable service : " + serviceName);
						JSONObject serviceConfig = new JSONObject(response.getString());
						String type = serviceConfig.getString("type");
						DistributableService newDS = DistributableService.instantiate(nodeCore, type, serviceConfig.getObject("config"));
						nodeCore.getFunctionManager().addFunction(serviceName, newDS, 10);
						ni = nodeCore.getDirectory().getNodeById(nodeCore.getNodeId());
						ni.addServiceInformation(serviceName, new ServiceInformation(serviceName));						
						logger.fine("Instantiated distributable service : " + serviceName);
					}
					else
					{
						logger.fine("No response received from 'firebus_distributable_services_source' ");
					}
				}
				catch(Exception e)
				{
					logger.severe("General error message when refreshing the source of a distributable function : " + e.getMessage());
				}
			}

			if(ni != null)
			{
				logger.info("Requesting Service");
				Message msg = new Message(ni.getNodeId(), nodeCore.getNodeId(), Message.MSGTYPE_REQUESTSERVICE, serviceName, requestPayload);
				Message resp = nodeCore.getCorrelationManager().synchronousCall(msg, subTimeout);
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
		
		if(responsePayload != null)
			return responsePayload;
		else
			throw new FunctionErrorException(errorMessage);
	}
	
}
