package com.nic.firebus;

import java.util.logging.Logger;

import com.nic.firebus.distributables.DistributableService;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.interfaces.ServiceProvider;
import com.nic.firebus.utils.JSONObject;

public class FunctionWorker extends Thread
{
	private Logger logger = Logger.getLogger("com.nic.firebus");
	protected FunctionEntry functionEntry;
	protected Message inboundMessage;
	protected NodeCore nodeCore;
	
	public FunctionWorker(FunctionEntry fe, Message im, NodeCore nc) //FunctionListener fl)
	{
		functionEntry = fe;
		inboundMessage = im;
		nodeCore = nc;
		setName("Firebus Function Worker");
		functionEntry.runStarted();
		start();
	}
	
	public void run()
	{
		Payload inPayload = inboundMessage.getPayload();

		if(functionEntry.function instanceof DistributableService)
		{
			DistributableService ds = (DistributableService)functionEntry.function;
			if(ds.isExpired())
			{
				logger.fine("Distributable service  " + functionEntry.serviceName + " has expired, refreshing the source");
				try
				{
					ServiceRequest request = new ServiceRequest(nodeCore, "firebus_distributable_services_source", new Payload(functionEntry.serviceName.getBytes()), 2000);
					Payload response = request.execute();
					if(response != null)
					{
						logger.fine("Refreshing distributable service : " + functionEntry.serviceName);
						JSONObject serviceConfig = new JSONObject(response.getString());
						String type = serviceConfig.getString("type");
						DistributableService newDS = DistributableService.instantiate(nodeCore, type, serviceConfig.getObject("config"));
						functionEntry.setFunction(newDS);
						logger.fine("Refreshed distributable service : " + functionEntry.serviceName);
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
		}
		
		if(inboundMessage.getType() == Message.MSGTYPE_REQUESTSERVICE  && functionEntry.function instanceof ServiceProvider)
		{
			logger.info("Executing Service Provider");
			Payload returnPayload = null;
			try
			{
				returnPayload = ((ServiceProvider)functionEntry.function).service(inPayload);
				Message msg = new Message(inboundMessage.getOriginatorId(), nodeCore.getNodeId(), Message.MSGTYPE_SERVICERESPONSE, inboundMessage.getSubject(), returnPayload);
				msg.setCorrelation(inboundMessage.getCorrelation());
				nodeCore.sendMessage(msg);
			}
			catch(FunctionErrorException e)
			{
				Message msg = new Message(inboundMessage.getOriginatorId(), nodeCore.getNodeId(), Message.MSGTYPE_SERVICEERROR, inboundMessage.getSubject(), new Payload(e.getMessage().getBytes()));
				msg.setCorrelation(inboundMessage.getCorrelation());
				nodeCore.sendMessage(msg);
			}
		}
		else if(inboundMessage.getType() == Message.MSGTYPE_PUBLISH  &&  functionEntry.function instanceof Consumer)
		{
			logger.info("Executing Consumer");
			((Consumer)functionEntry.function).consume(inPayload);
		}
		functionEntry.runEnded();
	}
}
