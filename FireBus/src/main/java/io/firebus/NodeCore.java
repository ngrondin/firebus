package io.firebus;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import io.firebus.discovery.DefaultDiscoveryAgent;


public class NodeCore
{
	private Logger logger = Logger.getLogger("io.firebus");
	protected int nodeId;
	protected boolean quit;
	protected String networkName;

	protected ConnectionManager connectionManager;
	protected FunctionManager functionManager;
	protected Directory directory;
	protected List<DiscoveryAgent> discoveryAgents;
	protected CorrelationManager correlationManager;
	protected ThreadManager threadManager;
	protected Cipher cipher;
	protected HistoryQueue historyQueue;
	
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
			nodeId = rnd.nextInt(2147483647);
			quit = false;
			networkName = n;	
			directory = new Directory();
			directory.getOrCreateNodeInformation(nodeId);
			connectionManager = new ConnectionManager(this, nodeId, networkName,  new SecretKeySpec(pw.getBytes(), "AES"), port);
			functionManager = new FunctionManager(this);
			correlationManager = new CorrelationManager(this);
			threadManager = new ThreadManager(this);
			historyQueue = new HistoryQueue(256);
			discoveryAgents = new ArrayList<DiscoveryAgent>();
			discoveryAgents.add(new DefaultDiscoveryAgent(this));
			logger.info("Initialised firebus node " + nodeId + " on " + networkName);
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
		threadManager.close();
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

	public FunctionManager getFunctionManager()
	{
		return functionManager;
	}
	
	public ConnectionManager getConnectionManager()
	{
		return connectionManager;
	}
	
	public CorrelationManager getCorrelationManager()
	{
		return correlationManager;
	}
	
	public ThreadManager getThreadManager() 
	{
		return threadManager;
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
	
	protected void forkThenRoute(Message msg)
	{
		threadManager.process(msg);
	}
	
	protected void route(Message msg)
	{
		long msgUID = (msg.getOriginatorId() << 32) + msg.getid();
		if(msg != null && historyQueue.check(msgUID))
		{
			logger.finest("\"****Routing**************\r\n" + msg + "\"");
			int destinationNodeId = msg.getDestinationId();
						
			if(destinationNodeId == nodeId  ||  destinationNodeId == 0)
				process(msg);

			if(destinationNodeId != nodeId  ||  destinationNodeId == 0)
				connectionManager.sendMessage(msg);
		}	
		logger.finer("Finished Routing Message " + msg.getid());
	}
	
	protected void process(Message msg)
	{
		if(msg != null)
		{
			logger.finest("Processing Message " + msg.getid());		
			if(msg.getDestinationId() == 0  ||  msg.getDestinationId() == nodeId)
			{
				switch(msg.getType())
				{
					case Message.MSGTYPE_QUERYNODE:
						processNodeInformationRequest(msg);
						break;
					case Message.MSGTYPE_NODEINFORMATION:
						directory.processNodeInformation(new String(msg.getPayload().data));
						correlationManager.receiveResponse(msg);
						break;
					case Message.MSGTYPE_GETFUNCTIONINFORMATION:
						functionManager.processServiceInformationRequest(msg);
						break;
					case Message.MSGTYPE_SERVICEINFORMATION:
						directory.processServiceInformation(msg.getOriginatorId(), msg.getSubject(), msg.getPayload().data);
						correlationManager.receiveResponse(msg);
						break;
					case Message.MSGTYPE_REQUESTSERVICE:
						functionManager.executeFunction(msg);
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
					case Message.MSGTYPE_SERVICEUNAVAILABLE:
						correlationManager.receiveResponse(msg);
						break;
					case Message.MSGTYPE_PUBLISH:
						if(functionManager.hasFunction(msg.getSubject()))
							functionManager.executeFunction(msg);
						break;
				}
			}
			logger.finer("Finished Processing Message " + msg.getid());		
		}
	}
	
	protected void processNodeInformationRequest(Message reqMsg)
	{
		logger.finer("Responding to a node information request");
		StringBuilder sb = new StringBuilder();
		sb.append(connectionManager.getAddressStateString(nodeId));
		sb.append(functionManager.getFunctionStateString(nodeId));
		sb.append(directory.getDirectoryStateString(nodeId));
		Message msg = new Message(reqMsg.getOriginatorId(), nodeId, Message.MSGTYPE_NODEINFORMATION, null, new Payload(null, sb.toString().getBytes()));
		msg.setCorrelation(reqMsg.getCorrelation());
		route(msg);
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Node Id :" + nodeId + "\r\n");
		sb.append("-------Functions----------\r\n");
		sb.append(functionManager);
		sb.append("-------Connections--------\r\n");
		sb.append(connectionManager);
		sb.append("-------Directory----------\r\n");
		sb.append(directory);
		sb.append("\r\n");
		return sb.toString();
	}



}
