package com.nic.firebus;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


public class NodeCore
{
	private Logger logger = Logger.getLogger("com.nic.firebus");
	protected int nodeId;
	protected boolean quit;
	protected String networkName;
	protected long lastConnectionMaintenance;
	//protected MessageQueue queue;
	//protected MessageQueue outboundQueue;
	protected ConnectionManager connectionManager;
	protected FunctionManager functionManager;
	protected Directory directory;
	protected DiscoveryManager discoveryManager;
	protected CorrelationManager correlationManager;
	protected ThreadManager threadManager;
	protected ArrayList<Address> knownAddresses;
	protected Cipher cipher;
	
	public NodeCore()
	{
		initialise(0, "firebus", "firebuspassword0");
	}
	
	public NodeCore(int p)
	{
		initialise(p, "firebus", "firebuspassword0");
	}

	public NodeCore(String network, String password)
	{
		initialise(0, network, password);
	}

	public NodeCore(int p, String network, String password)
	{
		initialise(p, network, password);
	}
	
	protected void initialise(int port, String n, String pw)
	{
		try
		{
			Random rnd = new Random();
			nodeId = rnd.nextInt();
			quit = false;
			networkName = n;	
			//queue = new MessageQueue();
			//outboundQueue = new MessageQueue();
			directory = new Directory();
			directory.getOrCreateNodeInformation(nodeId);
			connectionManager = new ConnectionManager(this, nodeId, networkName,  new SecretKeySpec(pw.getBytes(), "AES"), port);
			discoveryManager = new DiscoveryManager(this, nodeId, networkName, connectionManager.getPort());
			functionManager = new FunctionManager(this);
			correlationManager = new CorrelationManager(this);
			threadManager = new ThreadManager(this);
			//knownAddresses = new ArrayList<Address>();
						
			//start();			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void close()
	{
		connectionManager.close();
		discoveryManager.close();
		correlationManager.close();
		threadManager.close();
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
	
	public CorrelationManager getCorrelationManager()
	{
		return correlationManager;
	}
	
	public void addKnownNodeAddress(String a, int p)
	{
		connectionManager.addKnownNodeAddress(a, p);
	}
	
	protected void forkThenRoute(Message msg)
	{
		threadManager.startThread(msg);
	}
	
	protected void route(Message msg)
	{
		if(msg != null)
		{
			logger.finer("\"****Routing**************\r\n" + msg + "\"");
			int destinationNodeId = msg.getDestinationId();
			
			if(destinationNodeId == nodeId  ||  destinationNodeId == 0)
				process(msg);

			if(destinationNodeId != nodeId  ||  destinationNodeId == 0)
				connectionManager.sendMessage(msg);
		}	
		logger.fine("Finished Routing Message");
	}
	
	protected void process(Message msg)
	{
		if(msg != null)
		{
			logger.fine("Processing Message " + msg.getid());		
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
						functionManager.executeFunction(msg);
						break;
				}
			}
			logger.fine("Finished Processing Message " + msg.getid());		
		}
	}
	
	protected void processNodeInformationRequest(Message reqMsg)
	{
		logger.fine("Responding to a node information request");
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
