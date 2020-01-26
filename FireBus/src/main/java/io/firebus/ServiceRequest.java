package io.firebus;

import java.util.logging.Logger;

import io.firebus.distributables.DistributableService;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.information.NodeInformation;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.ServiceRequestor;
import io.firebus.utils.DataMap;

public class ServiceRequest extends Thread
{
	private Logger logger = Logger.getLogger("com.nic.firebus");
	protected NodeCore nodeCore;
	protected String serviceName;
	protected Payload requestPayload;
	protected int subTimeout;
	protected int requestTimeout;
	protected long expiry;
	protected ServiceRequestor requestor;
	protected String errorMessage;

	public ServiceRequest(NodeCore nc, String sn, Payload p, int t)
	{
		nodeCore = nc;
		serviceName = sn;
		requestPayload = p;
		requestTimeout = t;
		subTimeout = 500;
		errorMessage = null;
		expiry = System.currentTimeMillis() + requestTimeout;
	}
	
	public void execute(ServiceRequestor r)
	{
		requestor = r;
		start();
	}
	
	public void run()
	{
		setName("fbServiceReq" + getId());
		try
		{
			Payload responsePayload = execute();
			requestor.requestCallback(responsePayload);
		}
		catch(FunctionErrorException e)
		{
			requestor.requestErrorCallback(e);
		}
		catch(FunctionTimeoutException e)
		{
			requestor.requestTimeout();
		}
	}
	
	public Payload execute() throws FunctionErrorException, FunctionTimeoutException
	{
		logger.info("Requesting Service");
		Payload responsePayload = null;
		while(responsePayload == null  &&  System.currentTimeMillis() < expiry)
		{
			NodeInformation ni = nodeCore.getDirectory().findServiceProvider(serviceName);
			if(ni == null)
			{
				logger.fine("Broadcasting Service Information Request Message");
				Message findMsg = new Message(0, nodeCore.getNodeId(), Message.MSGTYPE_GETFUNCTIONINFORMATION, serviceName, null);
				Message respMsg = nodeCore.getCorrelationManager().sendRequestAndWait(findMsg, subTimeout);
				if(respMsg != null)
				{
					ni = nodeCore.getDirectory().getNodeById(respMsg.getOriginatorId());
				}
			}
			
			if(ni == null  &&  !serviceName.equals("firebus_distributable_services_source"))
			{
				try
				{
					logger.fine("Trying to retreive distributable service");
					ServiceRequest request = new ServiceRequest(nodeCore, "firebus_distributable_services_source", new Payload(serviceName.getBytes()), subTimeout * 2);
					Payload response = request.execute();
					if(response != null)
					{
						logger.fine("Instantiating distributable service : " + serviceName);
						DataMap serviceConfig = new DataMap(response.getString());
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
					logger.severe("General error when refreshing the source of a distributable function : " + e.getMessage());
				}
			}

			if(ni != null)
			{
				logger.fine("Sending service request message to " + ni.getNodeId());
				Message reqMsg = new Message(ni.getNodeId(), nodeCore.getNodeId(), Message.MSGTYPE_REQUESTSERVICE, serviceName, requestPayload);
				int correlation = nodeCore.getCorrelationManager().sendRequest(reqMsg, subTimeout);
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
						else if(respMsg.getType() == Message.MSGTYPE_SERVICEUNAVAILABLE)
						{
							ni.getServiceInformation(serviceName).reduceRating();
							break;
						} 
						else if(respMsg.getType() == Message.MSGTYPE_SERVICERESPONSE)
						{
							responsePayload = respMsg.getPayload();
							break;
						}
						else if(respMsg.getType() == Message.MSGTYPE_PROGRESS)
						{
							respMsg = nodeCore.getCorrelationManager().waitForResponse(correlation, requestTimeout);
						}
						
						if(System.currentTimeMillis() > expiry)
						{
							logger.info("Service request " + serviceName + " has timed out while executing");
							throw new FunctionTimeoutException("Service " + serviceName + " timed out while executing");
						}
					}
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
			throw new FunctionTimeoutException("Service " + serviceName + " could not be found");
	}
	
}
