package com.nic.firebus;

import java.util.logging.Logger;

import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ConsumerInformation;
import com.nic.firebus.information.NodeInformation;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.interfaces.ServiceProvider;
import com.nic.firebus.interfaces.ServiceRequestor;

public class Firebus
{
	private Logger logger = Logger.getLogger("com.nic.firebus");
	protected NodeCore nodeCore;
	
	public Firebus()
	{
		nodeCore = new NodeCore(0, "firebus", "firebuspassword0");
	}
	
	public Firebus(int p)
	{
		nodeCore = new NodeCore(p, "firebus", "firebuspassword0");
	}
	
	public Firebus(String network, String password)
	{
		nodeCore = new NodeCore(network, password);
	}

	public Firebus(int p, String network, String password)
	{
		nodeCore = new NodeCore(p, network, password);
	}

	public void addKnownNodeAddress(String a, int p)
	{
		nodeCore.addKnownNodeAddress(a, p);
	}
	
	public void registerServiceProvider(ServiceInformation serviceInformation, ServiceProvider serviceProvider, int maxConcurrent)
	{
		nodeCore.getFunctionManager().addFunction(serviceInformation, serviceProvider, maxConcurrent);
	}
	
	public void registerConsumer(ConsumerInformation consumerInfomation, Consumer consumer, int maxConcurrent)
	{
		nodeCore.getFunctionManager().addFunction(consumerInfomation, consumer, maxConcurrent);
	}
	
	public ServiceInformation getServiceInformation(String serviceName)
	{
		ServiceInformation si = null;
		if(nodeCore.getFunctionManager().hasFunction(serviceName))
		{
			si = nodeCore.getFunctionManager().getServiceInformation(serviceName);
		}
		else
		{
			NodeInformation ni = nodeCore.getDirectory().findServiceProvider(serviceName);
			if(ni == null)
			{
				logger.fine("Broadcasting Service Information Request Message");
				Message findMsg = new Message(0, nodeCore.getNodeId(), Message.MSGTYPE_GETFUNCTIONINFORMATION, serviceName, null);
				Message respMsg = nodeCore.getCorrelationManager().synchronousCall(findMsg, 2000);
				if(respMsg != null)
					si = nodeCore.getDirectory().getNodeById(respMsg.getOriginatorId()).getServiceInformation(serviceName);
			}	
		}

		return si;
	}

	public Payload requestService(String serviceName, Payload payload) throws FunctionErrorException
	{
		ServiceRequest request = new ServiceRequest(nodeCore, serviceName, payload, 2000, null);
		return request.waitForResponse();
	}

	public Payload requestService(String serviceName, Payload payload, int timeout) throws FunctionErrorException
	{
		ServiceRequest request = new ServiceRequest(nodeCore, serviceName, payload, timeout, null);
		return request.waitForResponse();
	}
	
	public void requestService(String serviceName, Payload payload, int timeout, ServiceRequestor requestor)
	{
		new ServiceRequest(nodeCore, serviceName, payload, timeout, requestor);
	}
	
	public void publish(String dataname, Payload payload)
	{
		logger.info("Publishing");
		nodeCore.sendMessage(new Message(0, nodeCore.getNodeId(), Message.MSGTYPE_PUBLISH, dataname, payload));
	}
	
	public void close()
	{
		nodeCore.close();
	}
}
