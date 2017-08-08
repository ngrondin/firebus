package com.nic.firebus;

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

	public byte[] requestService(String serviceName, byte[] payload)
	{
		return nodeCore.requestService(serviceName, payload, 2000);
	}

	public byte[] requestService(String serviceName, byte[] payload, int timeout)
	{
		return nodeCore.requestService(serviceName, payload, timeout);
	}
	
	public void requestService(String serviceName, byte[] payload, int timeout, ServiceRequestor requestor)
	{
		nodeCore.requestService(serviceName, payload, timeout, requestor);
	}
	
	public void publish(String dataname, byte[] payload)
	{
		nodeCore.publish(dataname, payload);
	}
}
