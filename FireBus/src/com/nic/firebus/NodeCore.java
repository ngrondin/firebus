package com.nic.firebus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

import javax.crypto.Cipher;

import com.nic.firebus.exceptions.FirebusFunctionException;
import com.nic.firebus.information.ConsumerInformation;
import com.nic.firebus.information.NodeInformation;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.interfaces.ConnectionListener;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.interfaces.DiscoveryListener;
import com.nic.firebus.interfaces.FunctionListener;
import com.nic.firebus.interfaces.ServiceProvider;

public class NodeCore extends Thread implements ConnectionListener, FunctionListener, DiscoveryListener
{
	private Logger logger = Logger.getLogger(NodeCore.class.getName());
	protected int nodeId;
	protected boolean quit;
	protected String network;
	protected String password;
	protected long lastAddressResolution;
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
			password = pw;
			network = n;			
			inboundQueue = new MessageQueue();
			outboundQueue = new MessageQueue();
			connectionManager = new ConnectionManager(port, this, pw);
			functionManager = new FunctionManager(this);
			directory = new Directory();
			discoveryManager = new DiscoveryManager(this, nodeId, connectionManager.getAddress());
			correlationManager = new CorrelationManager(outboundQueue);
			knownAddresses = new ArrayList<Address>();
			setName("Firebus Node");
			start();			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public int getNodeId()
	{
		return nodeId;
	}
	
	public void addKnownNodeAddress(String a, int p)
	{
		Address address = new Address(a, p);
		knownAddresses.add(address);
	}
	
	public void registerServiceProvider(ServiceInformation serviceInformation, ServiceProvider serviceProvider, int maxConcurrent)
	{
		functionManager.addFunction(serviceInformation, serviceProvider, maxConcurrent);
		advertiseTo(0);
	}
	
	public void registerConsumer(ConsumerInformation consumerInfomation, Consumer consumer, int maxConcurrent)
	{
		functionManager.addFunction(consumerInfomation, consumer, maxConcurrent);
	}
		
	public byte[] requestService(String serviceName, byte[] payload)
	{
		logger.info("Requesting Service");

		long expiry = System.currentTimeMillis() + 5000;
		Message respMsg = null;
		NodeInformation ni = null;

		while(respMsg == null  &&  System.currentTimeMillis() < expiry)
		{
			while((ni = directory.findServiceProvider(serviceName)) == null  &&  System.currentTimeMillis() < expiry)
			{
				logger.fine("Sending Find Service Message");
				Message findMsg = new Message(0, nodeId, Message.MSGTYPE_FINDSERVICE, serviceName, null);
				correlationManager.synchronousCall(findMsg, 2000);
			}
			
			if(ni != null)
			{
				logger.fine("Sending Request Message");
				Message msg = new Message(ni.getNodeId(), nodeId, Message.MSGTYPE_REQUESTSERVICE, serviceName, payload);
				respMsg = correlationManager.synchronousCall(msg, 2000);
				
				if(respMsg == null)
				{
					logger.fine("Setting node as unresponsive");
					ni.setUnresponsive();
				}
			}
		}

		if(respMsg != null)
		{
			logger.info("Returning Service Response");
			return respMsg.getPayload();
		}
		else
		{
			logger.info("No Response Received from Service Request");
			return null;
		}
	}		

	public void publish(String dataname, byte[] payload)
	{
		Message msg = new Message(0, nodeId, Message.MSGTYPE_PUBLISH, dataname, payload);
		outboundQueue.addMessage(msg);
	}
	
	public void connectionCreated(Connection c) 
	{
	}

	public void messageReceived(Message m, Connection c) 
	{
		logger.fine("Received Message");
		inboundQueue.addMessage(m);
	}

	public void connectionClosed(Connection c) 
	{
		logger.info("Connection Closed");
		NodeInformation ni = directory.getNodeByConnection(c);
		if(ni != null)
			ni.setConnection(null);
		connectionManager.removeConnection(c);
	}
	
	public void functionCallback(Message inboundMessage, byte[] payload) 
	{
		logger.fine("Function Returned");
		Message msg = new Message(inboundMessage.getOriginatorId(), nodeId, Message.MSGTYPE_SERVICERESPONSE, inboundMessage.getSubject(), payload);
		msg.setCorrelation(inboundMessage.getCorrelation());
		outboundQueue.addMessage(msg);
	}

	public void nodeDiscovered(int id, String address, int port)
	{
		Address a = new Address(address, port);
		NodeInformation ni = directory.getNodeById(id);
		if(ni == null)
		{
			NodeInformation nodeByAddress = directory.getNodeByAddress(a);
			if(nodeByAddress != null)
				directory.deleteNode(nodeByAddress);
			ni = new NodeInformation(id);
			ni.addAddress(a);
			directory.addNode(ni);
			logger.info("Node Discovered");
		}
		else if(!ni.containsAddress(a))
		{
			ni.addAddress(a);
		}
	}

	
	protected void resolveKnownAddresses()
	{
		for(int i = 0; i < knownAddresses.size(); i++)
		{
			try 
			{
				Connection connection = connectionManager.createConnection(knownAddresses.get(i));
				if(connection != null)
				{
					Message msg = new Message(0, nodeId, Message.MSGTYPE_CONNECT, null, getNodeStateString().getBytes());
					msg.setRepeatsLeft(0);
					msg.setConnection(connection);
					outboundQueue.addMessage(msg);
				}
			} 
			catch (IOException e) 
			{
			}
			knownAddresses.remove(i);
		}
	}

	protected void maintainConnectionCount()
	{
		ArrayList<NodeInformation> list = directory.getNodeToConnectTo();
		for(int i = 0; i < list.size(); i++)
		{
			NodeInformation ni = list.get(i);
			Connection c = connectionManager.obtainConnectionForNode(ni);
			if(c != null)
			{
				ni.setConnection(c);
				Message msg = new Message(ni.getNodeId(), nodeId, Message.MSGTYPE_CONNECT, null, getNodeStateString().getBytes());
				msg.setConnection(c);
				msg.setRepeatsLeft(0);
				outboundQueue.addMessage(msg);
			}
		}
	}
	
	protected void processNextInboundMessage()
	{
		logger.fine("Processing Inbound Message");
		Message msg = inboundQueue.getNextMessage();
		msg.decode();
		logger.finest("****Inbound****************\r\n" + msg);
		
		if(msg.getOriginatorId() != nodeId)
		{
			int originatorId = msg.getOriginatorId();
			int repeaterId = msg.getRepeaterId();
			int connectedNodeId = repeaterId == 0 ? originatorId : repeaterId;

			NodeInformation connectedNode = directory.getNodeById(connectedNodeId);
			if(connectedNode == null)
			{
				connectedNode = new NodeInformation(connectedNodeId);
				connectedNode.setConnection(msg.getConnection());
				directory.addNode(connectedNode);
			}
			else
			{
				if(connectedNode.getConnection() == null)
				{
					connectedNode.setConnection(msg.getConnection());
				}
				else
				{
					if(connectedNode.getConnection() != msg.getConnection())
					{
						logger.fine("duplicate connection detected");
						//TODO: There are 2 different connections, do something to fix
					}
				}
			}

			if(repeaterId != 0)
			{
				NodeInformation originatorNode = directory.getNodeById(originatorId);
				if(originatorNode == null)
				{
					originatorNode = new NodeInformation(originatorId);
					directory.addNode(originatorNode);
				}
				originatorNode.addRepeater(repeaterId);
			}
			
			if(msg.getDestinationId() == 0  ||  msg.getDestinationId() == nodeId)
			{
				switch(msg.getType())
				{
					case Message.MSGTYPE_CONNECT:
						directory.processStateMessage(new String(msg.getPayload()));
						advertiseTo(msg.getOriginatorId());
						break;
					case Message.MSGTYPE_NODESTATE:
						directory.processStateMessage(new String(msg.getPayload()));
						if(msg.getCorrelation() != 0)
							correlationManager.receiveResponse(msg);
						break;
					case Message.MSGTYPE_QUERYNODE:
						advertiseTo(msg.getOriginatorId());
						break;
					case Message.MSGTYPE_FINDSERVICE:
						if(functionManager.hasFunction(msg.getSubject()))
							advertiseTo(msg.getOriginatorId());
						break;
					case Message.MSGTYPE_REQUESTSERVICE:
						processServiceRequest(msg);
						break;
					case Message.MSGTYPE_SERVICERESPONSE:
						correlationManager.receiveResponse(msg);
						break;
					case Message.MSGTYPE_PUBLISH:
						processPublish(msg);
				}
			}
			
			if(msg.getDestinationId() == 0  ||  msg.getDestinationId() != nodeId)
			{
				if(msg.getRepeatCount() > 0)
					outboundQueue.addMessage(msg.repeat(nodeId));
			}
			
		}

		inboundQueue.deleteNextMessage();
		logger.fine("Finished Processing Inbound Message");
	}

	protected void processNextOutboundMessage()
	{
		logger.fine("Processing Outbound Message");
		Message msg = outboundQueue.getNextMessage();
		logger.finest("****Oubound**************\r\n" + msg);
		msg.encode();
		Connection c = msg.getConnection();
		if(c == null)
		{
			int destinationNodeId = msg.getDestinationId();
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
		}

		if(c == null)
			connectionManager.broadcastToAllConnections(msg);
		else
			c.sendMessage(msg);
		
		outboundQueue.deleteNextMessage();
		logger.fine("Finished Processing Outbound Message");
	}
	
	protected void processServiceRequest(Message msg)
	{
		try
		{
			functionManager.requestService(msg);
		}
		catch (FirebusFunctionException e)
		{
			Message outMsg = new Message(msg.getOriginatorId(), nodeId, Message.MSGTYPE_SERVICEUNAVAILABLE, msg.getSubject(), e.getMessage().getBytes());
			outMsg.setCorrelation(msg.getCorrelation());
			outboundQueue.addMessage(outMsg);
		}
	}
	
	protected void processPublish(Message msg)
	{
		try
		{
			functionManager.consume(msg);
		}
		catch (FirebusFunctionException e)
		{
			logger.severe(e.getMessage());
		}		
	}
	
	protected String getNodeStateString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(connectionManager.getAddressStateString(nodeId));
		sb.append(functionManager.getFunctionStateString(nodeId));
		sb.append(directory.getDirectoryStateString(nodeId));
		return sb.toString();
	}
	
	protected void advertiseTo(int destinationNodeId)
	{
		Message msg = new Message(destinationNodeId, nodeId, Message.MSGTYPE_NODESTATE, null, getNodeStateString().getBytes());
		outboundQueue.addMessage(msg);
	}
	

	
	public void run()
	{
		while(!quit)
		{
			try
			{
				resolveKnownAddresses();
				maintainConnectionCount();
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
					Thread.sleep(10);
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
