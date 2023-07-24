package io.firebus;

import java.util.List;

import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.information.NodeInformation;
import io.firebus.information.Statistics;
import io.firebus.interfaces.Consumer;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.interfaces.ServiceRequestor;
import io.firebus.interfaces.StreamProvider;
import io.firebus.logging.Logger;
import io.firebus.data.DataMap;

public class Firebus
{
	protected NodeCore nodeCore;
	protected int defaultTimeout = 10000;
	
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
	
	@Deprecated
	public void setThreadCount(int tc)
	{
		nodeCore.getServiceThreads().setThreadCount(tc);
	}
	
	public void setStreamThreadCount(int tc)
	{
		nodeCore.setStreamThreadCount(tc);
	}
	
	public void setServiceThreadCount(int tc)
	{
		nodeCore.setServiceThreadCount(tc);
	}
	
	public void setMessagingThreadCount(int tc)
	{
		nodeCore.setMessageThreadCount(tc);
	}

	public void setDefaultTimeout(int l)
	{
		defaultTimeout = l;
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
		nodeCore.getServiceManager().addService(serviceName, serviceProvider, maxConcurrent);
	}
	
	public void deregisterServiceProvider(String serviceName) 
	{
		nodeCore.getServiceManager().removeService(serviceName);
	}
	
	public void registerStreamProvider(String streamName, StreamProvider streamProvider, int maxConcurrent)
	{
		nodeCore.getStreamManager().addStream(streamName, streamProvider, maxConcurrent);
	}
	
	public void deregisterStreamProvider(String streamName) 
	{
		nodeCore.getStreamManager().removeStream(streamName);
	}
	
	public void registerConsumer(String consumerName, Consumer consumer, int maxConcurrent)
	{
		nodeCore.getConsumerManager().addConsumer(consumerName, consumer, maxConcurrent);
	}
	
	public void deregisterConsumer(String consumerName) 
	{
		nodeCore.getConsumerManager().removeConsumer(consumerName);
	}
	
	public boolean hasRegisteredFunction(String name) 
	{
		return nodeCore.getServiceManager().hasService(name) || nodeCore.getStreamManager().hasStream(name) || nodeCore.getConsumerManager().hasConsumer(name);
	}
	
	public boolean hasConnections()
	{
		return nodeCore.getConnectionManager().getConnectedNodeCount() > 0;
	}
	
	public NodeInformation getNodeInformation(int nodeId)
	{
		Logger.fine("fb.node.sendininforeq", null);
		Message queryMsg = new Message(nodeId, nodeCore.getNodeId(), Message.MSGTYPE_QUERYNODE, null, null);
		Message respMsg = nodeCore.getCorrelationManager().sendAndWait(queryMsg, 2000);
		if(respMsg != null)
			return nodeCore.getDirectory().getNodeById(nodeId);
		return null;
	}
	
	/*public FunctionInformation getFunctionInformation(String name)
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
	}*/

	public Payload requestService(String serviceName, Payload payload) throws FunctionErrorException, FunctionTimeoutException
	{
		ServiceRequest request = new ServiceRequest(nodeCore, serviceName, payload, defaultTimeout);
		return request.execute();
	}

	public Payload requestService(String serviceName, Payload payload, int timeout) throws FunctionErrorException, FunctionTimeoutException
	{
		ServiceRequest request = new ServiceRequest(nodeCore, serviceName, payload, timeout);
		return request.execute();
	}
	
	public void requestService(String serviceName, Payload payload, ServiceRequestor requestor, String requestorFunctionName, int timeout) throws FunctionErrorException, FunctionTimeoutException
	{
		ServiceRequestAsync request = new ServiceRequestAsync(nodeCore, serviceName, payload, requestor, requestorFunctionName, timeout);
		request.execute();
	}
	
	public void requestServiceAndForget(String serviceName, Payload payload) throws FunctionErrorException, FunctionTimeoutException
	{
		ServiceRequest request = new ServiceRequest(nodeCore, serviceName, payload, -1);
		request.execute();
	}

	public StreamEndpoint requestStream(String streamName, Payload payload, int timeout) throws FunctionErrorException, FunctionTimeoutException
	{
		return requestStream(streamName, payload, null, timeout);
	}

	public StreamEndpoint requestStream(String streamName, Payload payload, String requestorFunctionName, int timeout) throws FunctionErrorException, FunctionTimeoutException
	{
		StreamRequest stream = new StreamRequest(nodeCore, streamName, payload, requestorFunctionName, timeout);
		return stream.initiate();
	}
	
	public void publish(String dataname, Payload payload)
	{
		Logger.finer("fb.node.publishing");
		nodeCore.enqueue(new Message(0, nodeCore.getNodeId(), Message.MSGTYPE_PUBLISH, dataname, payload));
	}
	
	public void runAdhoc(Runnable runnable) 
	{
		nodeCore.getAdhocThreads().enqueue(runnable, null, defaultTimeout);
	}
	
	public List<Statistics> getStatistics() 
	{
		return nodeCore.getStatistics();
	}
	
	public DataMap getStatus()
	{
		return nodeCore.getStatus();
	}
	
	public int getNodeId()
	{
		return nodeCore.getNodeId();
	}
	
	public void close()
	{
		nodeCore.close();
	}
}
