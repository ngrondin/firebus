package io.firebus;

import java.util.logging.Logger;

import io.firebus.information.NodeInformation;

public class FunctionFinder {
	private static Logger logger = Logger.getLogger("io.firebus");
	protected static int subTimeout = 500;
	
	public static NodeInformation findFunction(NodeCore nodeCore, String functionName) 
	{
		NodeInformation ni = nodeCore.getDirectory().findFunction(functionName);
		if(ni == null)
		{
			logger.finer("Broadcasting Service Information Request Message");
			Message findMsg = new Message(0, nodeCore.getNodeId(), Message.MSGTYPE_GETFUNCTIONINFORMATION, functionName, null);
			Message respMsg = nodeCore.getCorrelationManager().sendAndWait(findMsg, subTimeout);
			if(respMsg != null)
			{
				ni = nodeCore.getDirectory().getNodeById(respMsg.getOriginatorId());
			}
		}
		/*
		if(ni == null  &&  !functionName.equals("firebus_distributable_services_source"))
		{
			try
			{
				logger.finer("Trying to retreive distributable service");
				ServiceRequest request = new ServiceRequest(nodeCore, "firebus_distributable_services_source", new Payload(functionName.getBytes()), subTimeout * 2);
				Payload response = request.execute();
				if(response != null)
				{
					logger.finer("Instantiating distributable service : " + functionName);
					DataMap serviceConfig = new DataMap(response.getString());
					String type = serviceConfig.getString("type");
					DistributableService newDS = DistributableService.instantiate(nodeCore, type, serviceConfig.getObject("config"));
					nodeCore.getServiceManager().addService(functionName, newDS, 10);
					ni = nodeCore.getDirectory().getNodeById(nodeCore.getNodeId());
					ni.addFunctionInformation(functionName, new ServiceInformation(functionName));						
					logger.finer("Instantiated distributable service : " + functionName);
				}
				else
				{
					logger.finer("No response received from 'firebus_distributable_services_source' ");
				}
			}
			catch(Exception e)
			{
				logger.finer("General error when refreshing the source of a distributable function : " + e.getMessage());
			}
		}*/
		return ni;
	}

}
