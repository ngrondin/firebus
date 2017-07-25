package com.nic.firebus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Node extends Thread
{
	//protected NodeInformation selfInformation;
	protected int nodeId;
	protected boolean quit;
	protected long lastAddressResolution;
	protected MessageQueue inboundQueue;
	protected MessageQueue outboundQueue;
	protected ConnectionManager connectionManager;
	protected FunctionManager functionManager;
	protected Directory directory;
	protected CorrelationManager correlationManager;
	protected ArrayList<Address> knownAddresses;
	
	protected class NodeConnectionListener implements ConnectionListener
	{
		public void connectionCreated(Connection c) 
		{
		}

		public void messageReceived(Message m, Connection c) 
		{
			inboundQueue.addMessage(m);
		}

		public void connectionClosed(Connection c) 
		{
			connectionManager.removeConnection(c);
		}
	}
	
	protected class NodeFunctionListener implements FunctionListener
	{
		public void functionCallback(int correlation, byte[] payload) 
		{
			Message originalMsg = correlationManager.getMessage(correlation);
			correlationManager.removeCorrelation(correlation);
			Message msg = new Message(originalMsg.getOriginatorId(), nodeId, 0, Message.MSGTYPE_SERVICERESPONSE, correlation, originalMsg.getSubject(), payload);
			outboundQueue.addMessage(msg);
		}
	}
	
	protected NodeConnectionListener nodeConnectionListener;
	protected NodeFunctionListener nodeFunctionListener;
	
	public Node()
	{
		initialise(null, 1991);
	}
	
	public Node(int p)
	{
		initialise(null, p);
	}
	
	protected void initialise(String cf, int port)
	{
		Random rnd = new Random();
		nodeId = rnd.nextInt();
		quit = false;
		inboundQueue = new MessageQueue();
		outboundQueue = new MessageQueue();
		nodeConnectionListener = new NodeConnectionListener();
		nodeFunctionListener = new NodeFunctionListener();
		connectionManager = new ConnectionManager(port, nodeConnectionListener);
		functionManager = new FunctionManager(nodeFunctionListener);
		directory = new Directory();
		correlationManager = new CorrelationManager();
		knownAddresses = new ArrayList<Address>();
		setName("Firebus Node " + nodeId);
		start();
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

	protected void resolveKnownAddresses()
	{
		//ArrayList<NodeInformation> nodes = directory.getNodesToDiscover();
		for(int i = 0; i < knownAddresses.size(); i++)
		{
			//NodeInformation nodeToResolve = nodes.get(i);
			//nodeToResolve.setLastDiscoverySentTime(System.currentTimeMillis());
			//Connection connection = connectionManager.obtainConnectionForNode(ni);
			try 
			{
				Connection connection = connectionManager.createConnection(knownAddresses.get(i));
				Message msg = new Message(0, nodeId, 0, Message.MSGTYPE_DISCOVER, 0, null, null);
				msg.setRepeatsLeft(0);
				msg.setConnection(connection);
				outboundQueue.addMessage(msg);
			} 
			catch (IOException e) 
			{
			}
			knownAddresses.remove(i);
		}
	}

	protected void processNextInboundMessage()
	{
		Message msg = inboundQueue.getNextMessage();
		msg.decode();
		
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
					case Message.MSGTYPE_ADVERTISE:
						directory.processAdvertisementMessage(msg.getPayload());
						break;
					case Message.MSGTYPE_DISCOVER:
						advertiseTo(msg.getOriginatorId(), null);
						break;
					case Message.MSGTYPE_FIND:
						if(functionManager.find(msg.getSubject()) != null)
							advertiseTo(msg.getOriginatorId(), msg.getSubject());
						break;
					case Message.MSGTYPE_REQUESTSERVICE:
						correlationManager.addMessage(msg.getCorrelation(), msg);
						functionManager.requestService(msg.getSubject(), msg.getPayload(), msg.getCorrelation());
						break;
					case Message.MSGTYPE_SERVICERESPONSE:
						correlationManager.addMessage(msg.getCorrelation(), msg);
						break;
				}
			}
			
			if(msg.getDestinationId() == 0  ||  msg.getDestinationId() != nodeId)
			{
				if(msg.getRepeatCount() > 0)
					outboundQueue.addMessage(msg.repeat(nodeId));
			}
			
			System.out.println("****Inbound****************\r\n" + msg);
		}

		inboundQueue.deleteNextMessage();
	}

	protected void processNextOutboundMessage()
	{
		Message msg = outboundQueue.getNextMessage();
		
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
		{
			connectionManager.broadcastToAllConnections(msg);
		}
		else
		{
			msg.encode();
			c.sendMessage(msg);
		}
		
		System.out.println("****Oubound**************\r\n" + msg);
		outboundQueue.deleteNextMessage();
	}
	
	protected void advertiseTo(int destinationNodeId, String functionName)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(connectionManager.getAddressAdvertisementString(nodeId));
		sb.append(functionManager.getFunctionAdvertisementString(nodeId, functionName));
		Message msg = new Message(destinationNodeId, nodeId, 0, Message.MSGTYPE_ADVERTISE, 0, null, sb.toString().getBytes());
		outboundQueue.addMessage(msg);
	}
	
	public void registerServiceProvider(String serviceName, ServiceProvider serviceProvider)
	{
		functionManager.addFunction(serviceName, serviceProvider);
		advertiseTo(0, serviceName);
	}
		
	public byte[] requestService(String serviceName, byte[] payload)
	{
		NodeInformation ni = directory.findServiceProvider(serviceName);
		if(ni == null)
		{
			Message msg = new Message(0, nodeId, 0, Message.MSGTYPE_FIND, 0, serviceName, null);
			outboundQueue.addMessage(msg);
			while((ni = directory.findServiceProvider(serviceName)) == null)
				try {sleep(10);} catch (Exception e) {}
		}
		
		int correlation = correlationManager.getNextCorrelation();
		Message msg = new Message(ni.getNodeId(), nodeId, 0, Message.MSGTYPE_REQUESTSERVICE, correlation, serviceName, payload);
		outboundQueue.addMessage(msg);
		
		while(!correlationManager.hasMessage(correlation))
			try {sleep(10);} catch (Exception e) {}

		byte[] returnPayload = correlationManager.getMessage(correlation).getPayload();
		correlationManager.removeCorrelation(correlation);
		return returnPayload;
	}		

	public void run()
	{
		while(!quit)
		{
			//long currentTime = System.currentTimeMillis();
			try
			{
				resolveKnownAddresses();
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
					sleep(10);
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
		sb.append("-------Directory----------\r\n");
		sb.append(directory);
		return sb.toString();
	}

}
