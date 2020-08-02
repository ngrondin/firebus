package io.firebus;

import java.util.logging.Logger;

import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.information.FunctionInformation;
import io.firebus.information.NodeInformation;
import io.firebus.interfaces.Consumer;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.interfaces.ServiceRequestor;
import io.firebus.interfaces.StreamProvider;

public class Firebus
{
	private Logger logger = Logger.getLogger("io.firebus");
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
	
	public void setThreadCount(int tc)
	{
		nodeCore.getThreadManager().setThreadCount(tc);
	}

	public void addKnownNodeAddress(String a, int p)
	{
		nodeCore.addKnownNodeAddress(a, p);
	}
	
	public void addDiscoveryAgent(DiscoveryAgent a)
	{
		nodeCore.addDiscoveryAgent(a);
	}
	
	public void registerServiceProvider(String serviceName, ServiceProvider serviceProvider, int maxConcurrent)
	{
		nodeCore.getFunctionManager().addFunction(serviceName, serviceProvider, maxConcurrent);
	}

	public void registerStreamProvider(String streamName, StreamProvider streamProvider, int maxConcurrent)
	{
		nodeCore.getFunctionManager().addFunction(streamName, streamProvider, maxConcurrent);
	}
	
	public void registerConsumer(String consumerName, Consumer consumer, int maxConcurrent)
	{
		nodeCore.getFunctionManager().addFunction(consumerName, consumer, maxConcurrent);
	}
	
	public boolean hasRegisteredFunction(String name) 
	{
		return nodeCore.getFunctionManager().hasFunction(name);
	}
	
	public NodeInformation getNodeInformation(int nodeId)
	{
		logger.fine("Sending Node Information Request Message");
		Message queryMsg = new Message(nodeId, nodeCore.getNodeId(), Message.MSGTYPE_QUERYNODE, null, null);
		Message respMsg = nodeCore.getCorrelationManager().sendAndWait(queryMsg, 2000);
		if(respMsg != null)
			return nodeCore.getDirectory().getNodeById(nodeId);
		return null;
	}
	
	public FunctionInformation getFunctionInformation(String name)
	{
		FunctionInformation si = null;
		NodeInformation ni = nodeCore.getDirectory().findFunction(name);
		if(ni == null)
		{
			logger.fine("Broadcasting Service Information Request Message");
			Message findMsg = new Message(0, nodeCore.getNodeId(), Message.MSGTYPE_GETFUNCTIONINFORMATION, name, null);
			Message respMsg = nodeCore.getCorrelationManager().sendAndWait(findMsg, 2000);
			if(respMsg != null)
				si = nodeCore.getDirectory().getNodeById(respMsg.getOriginatorId()).getFunctionInformation(name);
		}

		return si;
	}

	public Payload requestService(String serviceName, Payload payload) throws FunctionErrorException, FunctionTimeoutException
	{
		ServiceRequest request = new ServiceRequest(nodeCore, serviceName, payload, 10000);
		return request.execute();
	}

	public Payload requestService(String serviceName, Payload payload, int timeout) throws FunctionErrorException, FunctionTimeoutException
	{
		ServiceRequest request = new ServiceRequest(nodeCore, serviceName, payload, timeout);
		return request.execute();
	}
	
	public void requestService(String serviceName, Payload payload, int timeout, ServiceRequestor requestor)
	{
		ServiceRequest request = new ServiceRequest(nodeCore, serviceName, payload, timeout);
		request.execute(requestor);
	}
	
	public StreamEndpoint requestStream(String streamName, Payload payload, int timeout) throws FunctionErrorException, FunctionTimeoutException
	{
		StreamRequest stream = new StreamRequest(nodeCore, streamName, payload, timeout);
		return stream.initiate();
	}

	public void publish(String dataname, Payload payload)
	{
		logger.finer("Publishing");
		nodeCore.forkThenRoute(new Message(0, nodeCore.getNodeId(), Message.MSGTYPE_PUBLISH, dataname, payload));
	}
	
	public void close()
	{
		nodeCore.close();
	}
}
