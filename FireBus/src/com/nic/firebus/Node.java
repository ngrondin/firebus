package com.nic.firebus;

import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ConsumerInformation;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.interfaces.ServiceProvider;
import com.nic.firebus.interfaces.ServiceRequestor;

public class Node
{
	protected NodeCore nodeCore;
	
	public Node()
	{
		nodeCore = new NodeCore(0, "firebus", "firebuspassword0");
	}
	
	public Node(int p)
	{
		nodeCore = new NodeCore(p, "firebus", "firebuspassword0");
	}
	
	public Node(String network, String password)
	{
		nodeCore = new NodeCore(network, password);
	}

	public Node(int p, String network, String password)
	{
		nodeCore = new NodeCore(p, network, password);
	}

	public void addKnownNodeAddress(String a, int p)
	{
		nodeCore.addKnownNodeAddress(a, p);
	}
	
	public void registerServiceProvider(ServiceInformation serviceInformation, ServiceProvider serviceProvider, int maxConcurrent)
	{
		nodeCore.registerServiceProvider(serviceInformation, serviceProvider, maxConcurrent);
	}
	
	public void registerConsumer(ConsumerInformation consumerInfomation, Consumer consumer, int maxConcurrent)
	{
		nodeCore.registerConsumer(consumerInfomation, consumer, maxConcurrent);
	}
		
	public ServiceInformation getServiceInformation(String serviceName)
	{
		return nodeCore.getServiceInformation(serviceName, 2000);
	}

	public Payload requestService(String serviceName, Payload payload) throws FunctionErrorException
	{
		return nodeCore.requestService(serviceName, payload, 2000);
	}

	public Payload requestService(String serviceName, Payload payload, int timeout) throws FunctionErrorException
	{
		return nodeCore.requestService(serviceName, payload, timeout);
	}
	
	public void requestService(String serviceName, Payload payload, int timeout, ServiceRequestor requestor)
	{
		nodeCore.requestService(serviceName, payload, timeout, requestor);
	}
	
	public void publish(String dataname, Payload payload)
	{
		nodeCore.publish(dataname, payload);
	}
}
