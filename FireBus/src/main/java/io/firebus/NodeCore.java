package io.firebus;

import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.PBEKeySpec;

import io.firebus.discovery.DefaultDiscoveryAgent;
import io.firebus.information.Statistics;
import io.firebus.logging.Logger;
import io.firebus.threads.ThreadManager;
import io.firebus.data.DataMap;


public class NodeCore
{
	protected int nodeId;
	protected boolean quit;
	protected String networkName;

	protected ConnectionManager connectionManager;
	protected ServiceManager serviceManager;
	protected StreamManager streamManager;
	protected ConsumerManager consumerManager;
	protected Directory directory;
	protected List<DiscoveryAgent> discoveryAgents;
	protected CorrelationManager correlationManager;
	protected ThreadManager messageThreads;
	protected ThreadManager serviceThreads;
	protected ThreadManager streamThreads;
	protected ThreadManager adhocThreads;
	protected Cipher cipher;
	protected HistoryQueue historyQueue;
	
	protected byte[] salt = {(byte)98, (byte)17, (byte)213, (byte)33, (byte)198, (byte)234, (byte)38, (byte)87, (byte)251, (byte)194, (byte)67, (byte)71, (byte)9, (byte)54, (byte)201, (byte)12};
	
	protected NodeCore()
	{
		initialise(0, "firebus", "firebuspassword0");
	}
	
	protected NodeCore(int p)
	{
		initialise(p, "firebus", "firebuspassword0");
	}

	protected NodeCore(String network, String password)
	{
		initialise(0, network, password);
	}

	protected NodeCore(int p, String network, String password)
	{
		initialise(p, network, password);
	}
	
	protected void initialise(int port, String n, String pw)
	{
		try
		{
			Random rnd = new Random();
			SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(pw.toCharArray(), salt, 65536, 256);
			byte[] keyBytes = f.generateSecret(spec).getEncoded();
			SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

			nodeId = rnd.nextInt(2147483647);
			quit = false;
			networkName = n;	
			directory = new Directory();
			directory.getOrCreateNodeInformation(nodeId);
			connectionManager = new ConnectionManager(this, nodeId, networkName, key, port);
			serviceManager = new ServiceManager(this);
			streamManager = new StreamManager(this);
			consumerManager = new ConsumerManager(this);
			correlationManager = new CorrelationManager(this);
			messageThreads = new ThreadManager(this, 10, 10, "Mesg");
			serviceThreads = new ThreadManager(this, 50, 5, "Service");
			streamThreads = new ThreadManager(this, 10, 2, "Stream");
			adhocThreads = new ThreadManager(this, 2, 1, "adhoc");
			historyQueue = new HistoryQueue(256);
			discoveryAgents = new ArrayList<DiscoveryAgent>();
			discoveryAgents.add(new DefaultDiscoveryAgent(this));
			Logger.info("fb.node.init", new DataMap("nodeid", nodeId, "network", networkName));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void close()
	{
		connectionManager.close();
		correlationManager.close();
		messageThreads.close();
		serviceThreads.close();
		streamThreads.close();
		for(DiscoveryAgent agent : discoveryAgents)
			agent.close();
		quit = true;
		synchronized(this)
		{
			this.notifyAll();
		}
	}
	
	public int getNodeId()
	{
		return nodeId;
	}
	
	public String getNetworkName()
	{
		return networkName;
	}
	
	public Directory getDirectory()
	{
		return directory;
	}
	
	public ServiceManager getServiceManager() 
	{
		return serviceManager;
	}
	
	public StreamManager getStreamManager()
	{
		return streamManager;
	}
	
	public ConsumerManager getConsumerManager()
	{
		return consumerManager;
	}
	
	public ConnectionManager getConnectionManager()
	{
		return connectionManager;
	}
	
	public CorrelationManager getCorrelationManager()
	{
		return correlationManager;
	}
	
	public ThreadManager getServiceThreads() 
	{
		return serviceThreads;
	}
	
	public ThreadManager getStreamThreads() 
	{
		return streamThreads;
	}
	
	public ThreadManager getAdhocThreads() 
	{
		return adhocThreads;
	}
	
	public void setServiceThreadCount(int c)
	{
		serviceThreads.setThreadCount(c);
	}
	
	public void setStreamThreadCount(int c)
	{
		streamThreads.setThreadCount(c);
	}
	
	public void setMessageThreadCount(int c)
	{
		messageThreads.setThreadCount(c);
	}
	
	public void addKnownNodeAddress(String a, int p)
	{
		connectionManager.addKnownNodeAddress(a, p);
	}
	
	public void addDiscoveryAgent(DiscoveryAgent agent)
	{
		agent.setNodeCore(this);
		discoveryAgents.add(agent);
	}
	
	protected void enqueue(Message msg)
	{
		messageThreads.enqueue(new RouteMessage(this, msg), "msg", -1);
	}
	
	protected void route(Message msg)
	{
		long msgUID = (msg.getOriginatorId() << 32) + msg.getid();
		if(msg != null && historyQueue.check(msgUID))
		{
			int destinationNodeId = msg.getDestinationId();
			if(destinationNodeId == nodeId  ||  destinationNodeId == 0)
				process(msg);
			if(destinationNodeId != nodeId  ||  destinationNodeId == 0)
				connectionManager.sendMessage(msg);
		}
	}
	
	protected void process(Message msg)
	{
		if(msg != null)
		{	
			if(msg.getDestinationId() == 0  ||  msg.getDestinationId() == nodeId)
			{
				switch(msg.getType())
				{
					case Message.MSGTYPE_QUERYNODE:
						processNodeInformationRequest(msg);
						break;
					case Message.MSGTYPE_NODEINFORMATION:
						directory.processNodeInformation(msg.getPayload().getString());
						correlationManager.receiveResponse(msg);
						break;
					case Message.MSGTYPE_GETFUNCTIONINFORMATION:
						String name = msg.getSubject();
						if(serviceManager.hasService(name))
							serviceManager.processServiceInformationRequest(msg);
						else if(streamManager.hasStream(name))
							streamManager.processServiceInformationRequest(msg);
						else if(consumerManager.hasConsumer(name))
							consumerManager.processServiceInformationRequest(msg);
						break;
					case Message.MSGTYPE_FUNCTIONINFORMATION:
						directory.processFunctionInformation(msg.getOriginatorId(), msg.getSubject(), msg.getPayload().getBytes());
						correlationManager.receiveResponse(msg);
						break;
					case Message.MSGTYPE_REQUESTSERVICE:
						serviceManager.executeService(msg);
						break;
					case Message.MSGTYPE_REQUESTSERVICEANDFORGET:
						serviceManager.executeService(msg);
						break;
					case Message.MSGTYPE_PROGRESS:
						correlationManager.receiveResponse(msg);
						break;
					case Message.MSGTYPE_SERVICERESPONSE:
						correlationManager.receiveResponse(msg);
						break;
					case Message.MSGTYPE_SERVICEERROR:
						correlationManager.receiveResponse(msg);
						break;
					case Message.MSGTYPE_FUNCTIONUNAVAILABLE:
						correlationManager.receiveResponse(msg);
						break;
					case Message.MSGTYPE_PUBLISH:
						if(consumerManager.hasConsumer(msg.getSubject()))
							consumerManager.consume(msg);
						break;
					case Message.MSGTYPE_REQUESTSTREAM:
						streamManager.connectStream(msg);
						break;
					case Message.MSGTYPE_STREAMACCEPT:
						correlationManager.receiveResponse(msg);
						break;
					case Message.MSGTYPE_STREAMERROR:
						correlationManager.receiveResponse(msg);
						break;
					case Message.MSGTYPE_STREAMDATA:
						correlationManager.receiveResponse(msg);
						break;
					case Message.MSGTYPE_STREAMEND:
						correlationManager.receiveResponse(msg);
						break;
				}
			}	
		}
	}
	
	protected void processNodeInformationRequest(Message reqMsg)
	{
		Logger.finer("fb.node.inforeq.resp", null);
		StringBuilder sb = new StringBuilder();
		sb.append(connectionManager.getAddressStateString(nodeId));
		sb.append(serviceManager.getFunctionStateString(nodeId));
		sb.append(streamManager.getFunctionStateString(nodeId));
		sb.append(consumerManager.getFunctionStateString(nodeId));
		sb.append(directory.getDirectoryStateString(nodeId));
		Message msg = new Message(reqMsg.getOriginatorId(), nodeId, Message.MSGTYPE_NODEINFORMATION, null, new Payload(null, sb.toString().getBytes()));
		msg.setCorrelation(reqMsg.getCorrelation(), 0);
		route(msg);
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Node Id :" + nodeId + "\r\n");
		sb.append("-------Functions----------\r\n");
		sb.append(serviceManager);
		sb.append(streamManager);
		sb.append(consumerManager);
		sb.append("-------Connections--------\r\n");
		sb.append(connectionManager);
		sb.append("-------Directory----------\r\n");
		sb.append(directory);
		sb.append("\r\n");
		return sb.toString();
	}

	public List<Statistics> getStatistics() {
		List<Statistics> list = new ArrayList<Statistics>();
		list.addAll(serviceManager.getStatistics());
		list.addAll(streamManager.getStatistics());
		return list;
	}
	
	public DataMap getStatus()
	{
		DataMap status = new DataMap();
		status.put("nodeid", this.nodeId);
		status.put("connmgr", connectionManager.getStatus());
		status.put("directory", directory.getStatus());
		DataMap funcs = new DataMap();
		funcs.put("services", serviceManager.getStatus());
		funcs.put("streams", streamManager.getStatus());
		funcs.put("consumers", consumerManager.getStatus());
		status.put("functions", funcs);
		DataMap threads = new DataMap();
		threads.put("services", serviceThreads.getStatus());
		threads.put("streams", streamThreads.getStatus());
		threads.put("messaging", messageThreads.getStatus());
		status.put("threads", threads);
		status.put("correlation", correlationManager.getStatus());
		return status;
	}

}
