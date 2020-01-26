package io.firebus;

import java.util.logging.Logger;

import io.firebus.distributables.DistributableService;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.interfaces.Consumer;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.utils.DataMap;

public class FunctionWorker extends Thread
{
	private Logger logger = Logger.getLogger("com.nic.firebus");
	protected FunctionEntry functionEntry;
	protected Message inboundMessage;
	protected NodeCore nodeCore;
	
	public FunctionWorker(FunctionEntry fe, Message im, NodeCore nc)
	{
		functionEntry = fe;
		inboundMessage = im;
		nodeCore = nc;
		setName("fbWorker" + getId());
		functionEntry.runStarted();
		start();
	}
	
	public void run()
	{
		/*
		Payload inPayload = inboundMessage.getPayload();

		if(inboundMessage.getType() == Message.MSGTYPE_REQUESTSERVICE  && functionEntry.function instanceof ServiceProvider)
		{
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
			
			logger.info("Executing Service Provider (correlation: " + inboundMessage.getCorrelation() + ")");
			Payload returnPayload = null;
			try
			{
				Message progressMsg = new Message(inboundMessage.getOriginatorId(), nodeCore.getNodeId(), Message.MSGTYPE_SERVICEPROGRESS, inboundMessage.getSubject(), null);
				progressMsg.setCorrelation(inboundMessage.getCorrelation());
				nodeCore.sendMessage(progressMsg);

				returnPayload = ((ServiceProvider)functionEntry.function).service(inPayload);
				
				Message responseMsg = new Message(inboundMessage.getOriginatorId(), nodeCore.getNodeId(), Message.MSGTYPE_SERVICERESPONSE, inboundMessage.getSubject(), returnPayload);
				responseMsg.setCorrelation(inboundMessage.getCorrelation());
				nodeCore.sendMessage(responseMsg);
			}
			catch(FunctionErrorException e)
			{
				Throwable t = e;
				String errorMessage = "";
				while(t != null)
				{
					if(errorMessage.length() > 0)
						errorMessage += " : ";
					errorMessage += t.getMessage();
					t = t.getCause();
				}
				Message msg = new Message(inboundMessage.getOriginatorId(), nodeCore.getNodeId(), Message.MSGTYPE_SERVICEERROR, inboundMessage.getSubject(), new Payload(errorMessage.getBytes()));
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
		*/
	}
}
