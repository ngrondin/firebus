package com.nic.firebus;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.nic.firebus.information.NodeInformation;
import com.nic.firebus.interfaces.DiscoveryListener;

public class NodeCore extends Thread implements DiscoveryListener
{
	private Logger logger = Logger.getLogger("com.nic.firebus");
	protected int nodeId;
	protected boolean quit;
	protected String networkName;
	protected long lastConnectionMaintenance;
	protected MessageQueue inboundQueue;
	protected MessageQueue outboundQueue;
	protected ConnectionManager connectionManager;
	protected FunctionManager functionManager;
	protected Directory directory;
	protected DiscoveryManager discoveryManager;
	protected CorrelationManager correlationManager;
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
			inboundQueue = new MessageQueue();
			outboundQueue = new MessageQueue();
			directory = new Directory();
			directory.getOrCreateNodeInformation(nodeId);
			connectionManager = new ConnectionManager(this, nodeId, networkName,  new SecretKeySpec(pw.getBytes(), "AES"), port);
			discoveryManager = new DiscoveryManager(this, nodeId, networkName, connectionManager.getPort());
			functionManager = new FunctionManager(this);
			correlationManager = new CorrelationManager(this);
			knownAddresses = new ArrayList<Address>();
			setName("Firebus Node");
			
			start();			
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
		Address address = new Address(a, p);
		knownAddresses.add(address);
		synchronized(this)
		{
			this.notifyAll();
		}
	}
		
	public void sendMessage(Message msg)
	{
		outboundQueue.addMessage(msg);
		synchronized(this)
		{
			this.notifyAll();
		}
	}
	
	public void messageReceived(Message m, Connection c) 
	{
		int originatorId = m.getOriginatorId();
		int connectedId = c.getRemoteNodeId();
		if(originatorId != nodeId)
		{
			logger.fine("Received message from node " + c.getRemoteNodeId());
			if(connectedId != originatorId)
			{
				NodeInformation originatorNode = directory.getOrCreateNodeInformation(originatorId);
				originatorNode.addRepeater(connectedId);
			}
			inboundQueue.addMessage(m);
			synchronized(this)
			{
				this.notifyAll();
			}
		}
		else
		{
			logger.fine("Blocked message from self");
		}
	}

	public void nodeDiscovered(int id, Address address)
	{
		logger.fine("Node discovered : " + id + " at address " + address);
		directory.processDiscoveredNode(id,  address);
		lastConnectionMaintenance = 0;
		synchronized(this)
		{
			this.notifyAll();
		}

	}

	protected void maintainConnectionCount()
	{
		int i1 = 0;
		int i2 = 0;
		logger.finest("Maintaining connection counts");
		while(connectionManager.getConnectionCount() < 3  &&  !(i1 >= knownAddresses.size()  &&  i2 >= directory.getNodeCount()))
		{
			if(i1 < knownAddresses.size())
			{
				Address a = knownAddresses.get(i1);
				if(connectionManager.getConnectionByAddress(a) == null)
				{
					try
					{
						connectionManager.createConnection(a);
					}
					catch(Exception e)
					{
						logger.fine(e.getMessage());
					}
				}
				i1++;
			}
			else if(i2 < directory.getNodeCount())
			{
				NodeInformation ni = directory.getNode(i2);
				if(connectionManager.getConnectionByNodeId(ni.getNodeId()) == null  &&  ni.getAddressCount() > 0  &&  !ni.isUnconnectable())
					connectionManager.obtainConnectionForNode(ni);
				i2++;
			}
		}
		lastConnectionMaintenance = System.currentTimeMillis();
	}
	
	protected void processNextInboundMessage()
	{
		logger.fine("Processing Inbound Message");
		Message msg = inboundQueue.getNextMessage();
		logger.finer("****Inbound****************\r\n" + msg);
		
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
				case Message.MSGTYPE_SERVICEPROGRESS:
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

		if(msg.getDestinationId() == 0  ||  msg.getDestinationId() != nodeId)
		{
			if(msg.getRepeatsLeft() > 0)
				sendMessage(msg.repeat());
		}			

		inboundQueue.deleteNextMessage();
		logger.fine("Finished Processing Inbound Message");
	}

	protected void processNextOutboundMessage()
	{
		logger.fine("Processing Outbound Message");
		Message msg = outboundQueue.getNextMessage();
		logger.finer("****Oubound**************\r\n" + msg);
		Connection c = null;
		int destinationNodeId = msg.getDestinationId();
		int originatorNodeId = msg.getOriginatorId();
		
		if(originatorNodeId == nodeId  &&  (destinationNodeId == nodeId  ||  destinationNodeId == 0))
		{
			inboundQueue.addMessage(msg);
		}

		if(destinationNodeId != nodeId  ||  destinationNodeId == 0)
		{
			if(destinationNodeId != 0)
			{
				NodeInformation ni = directory.getNodeById(destinationNodeId);
				if(ni != null)
				{
					c = connectionManager.obtainConnectionForNode(ni);
					if(c == null)
					{
						int rpt = ni.getRandomRepeater();
						if(rpt != 0)
						{
							ni = directory.getNodeById(rpt);
							c = connectionManager.obtainConnectionForNode(ni);
						}
					}							
				}
			}

			if(c == null)
				connectionManager.broadcastToAllConnections(msg);
			else
				c.sendMessage(msg);
		}
		
		outboundQueue.deleteNextMessage();
		logger.fine("Finished Processing Outbound Message");
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
		sendMessage(msg);
	}
	
	public void run()
	{
		while(!quit)
		{
			//logger.finest("Doing a control cycle");
			try
			{
				correlationManager.checkExpiredCalls();
				if(lastConnectionMaintenance < System.currentTimeMillis() - 1000)
				{
					maintainConnectionCount();
				}
				if(inboundQueue.getMessageCount() > 0)
				{
					processNextInboundMessage();
				}
				else if(outboundQueue.getMessageCount() > 0)
				{
					processNextOutboundMessage();
				}
				else
				{
					synchronized(this)
					{
						this.wait(100);
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
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
