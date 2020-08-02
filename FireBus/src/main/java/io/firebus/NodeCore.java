package io.firebus;

import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.PBEKeySpec;

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
					case Message.MSGTYPE_FUNCTIONINFORMATION:
						directory.processFunctionInformation(msg.getOriginatorId(), msg.getSubject(), msg.getPayload().data);
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
					case Message.MSGTYPE_FUNCTIONUNAVAILABLE:
						correlationManager.receiveResponse(msg);
						break;
					case Message.MSGTYPE_PUBLISH:
						if(functionManager.hasFunction(msg.getSubject()))
							functionManager.executeFunction(msg);
						break;
					case Message.MSGTYPE_REQUESTSTREAM:
						functionManager.executeFunction(msg);
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
		msg.setCorrelation(reqMsg.getCorrelation(), 0);
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
