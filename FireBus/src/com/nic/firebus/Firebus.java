package com.nic.firebus;

import java.util.logging.Logger;

import com.nic.firebus.exceptions.FunctionErrorException;
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
	
	public Firebus(NodeCore nc)
	{
		nodeCore = nc;
	}

	public void addKnownNodeAddress(String a, int p)
	{
		nodeCore.addKnownNodeAddress(a, p);
	}
	
	public void registerServiceProvider(String serviceName, ServiceProvider serviceProvider, int maxConcurrent)
	{
		nodeCore.getFunctionManager().addFunction(serviceName, serviceProvider, maxConcurrent);
	}
	
	public void registerConsumer(String consumerName, Consumer consumer, int maxConcurrent)
	{
		nodeCore.getFunctionManager().addFunction(consumerName, consumer, maxConcurrent);
	}
	
	public NodeInformation getNodeInformation(int nodeId)
	{
		logger.fine("Sending Node Information Request Message");
		Message queryMsg = new Message(nodeId, nodeCore.getNodeId(), Message.MSGTYPE_QUERYNODE, null, null);
		Message respMsg = nodeCore.getCorrelationManager().synchronousCall(queryMsg, 2000);
		if(respMsg != null)
			return nodeCore.getDirectory().getNodeById(nodeId);
		return null;
	}
	
	public ServiceInformation getServiceInformation(String serviceName)
	{
		ServiceInformation si = null;
		NodeInformation ni = nodeCore.getDirectory().findServiceProvider(serviceName);
		if(ni == null)
		{
			logger.fine("Broadcasting Service Information Request Message");
			Message findMsg = new Message(0, nodeCore.getNodeId(), Message.MSGTYPE_GETFUNCTIONINFORMATION, serviceName, null);
			Message respMsg = nodeCore.getCorrelationManager().synchronousCall(findMsg, 2000);
			if(respMsg != null)
				si = nodeCore.getDirectory().getNodeById(respMsg.getOriginatorId()).getServiceInformation(serviceName);
		}

		return si;
	}

	public Payload requestService(String serviceName, Payload payload) throws FunctionErrorException
	{
		ServiceRequest request = new ServiceRequest(nodeCore, serviceName, payload, 2000);
		return request.execute();
	}

	public Payload requestService(String serviceName, Payload payload, int timeout) throws FunctionErrorException
	{
		ServiceRequest request = new ServiceRequest(nodeCore, serviceName, payload, timeout);
		return request.execute();
	}
	
	public void requestService(String serviceName, Payload payload, int timeout, ServiceRequestor requestor)
	{
		ServiceRequest request = new ServiceRequest(nodeCore, serviceName, payload, timeout);
		request.execute(requestor);
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
