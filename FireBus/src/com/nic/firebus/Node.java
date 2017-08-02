package com.nic.firebus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Node
{
	protected class NodeConnectionListener implements ConnectionListener
	{
		public void connectionCreated(Connection c) 
		{
		}

		public void messageReceived(Message m, Connection c) 
		{
			if(verbose == 2)
				System.out.println("Received Message");
			inboundQueue.addMessage(m);
		}

		public void connectionClosed(Connection c) 
		{
			if(verbose == 2)
				System.out.println("Connection Closed");
			NodeInformation ni = directory.getNodeByConnection(c);
			if(ni != null)
				ni.setConnection(null);
			connectionManager.removeConnection(c);
		}
	}
	
	protected class NodeFunctionListener implements FunctionListener
	{
		public void functionCallback(int correlation, byte[] payload) 
		{
			if(verbose == 2)
				System.out.println("Function Returned");
			Message originalMsg = correlationManager.getMessage(correlation);
			correlationManager.removeCorrelation(correlation);
			Message msg = new Message(originalMsg.getOriginatorId(), nodeId, 0, Message.MSGTYPE_SERVICERESPONSE, correlation, originalMsg.getSubject(), payload);
			outboundQueue.addMessage(msg);
		}
	}
	
	protected class NodeDiscoveryListener implements DiscoveryListener
	{
		public void nodeDiscovered(int id, String address, int port)
		{
			if(verbose == 2)
				System.out.println("Node Discovered");
			Address a = new Address(address, port);
			NodeInformation ni = directory.getNodeById(id);
			if(ni == null)
			{
				ni = new NodeInformation(id);
				directory.addNode(ni);
			}
			if(!ni.containsAddress(a))
			{
				ni.addAddress(a);
			}
		}
	}
	
	protected class NodeControlLoop extends Thread
	{
		public void run()
		{
			while(!quit)
			{
				controlLoop();
			}
		}
	}
	
	protected int nodeId;
	protected boolean quit;
	protected int verbose;
	protected long lastAddressResolution;
	protected MessageQueue inboundQueue;
	protected MessageQueue outboundQueue;
	protected ConnectionManager connectionManager;
	protected FunctionManager functionManager;
	protected Directory directory;
	protected DiscoveryManager discoveryManager;
	protected CorrelationManager correlationManager;
	protected NodeConnectionListener nodeConnectionListener;
	protected NodeFunctionListener nodeFunctionListener;
	protected NodeDiscoveryListener nodeDiscoveryListener;
	protected ArrayList<Address> knownAddresses;
	protected NodeControlLoop nodeControlLoop;
	
	public Node()
	{
		initialise(0);
	}
	
	public Node(int p)
	{
		initialise(p);
	}
	
	protected void initialise(int port)
	{
		try
		{
			Random rnd = new Random();
			nodeId = rnd.nextInt();
			verbose = 2;
			quit = false;
			nodeConnectionListener = new NodeConnectionListener();
			nodeFunctionListener = new NodeFunctionListener();
			nodeDiscoveryListener = new NodeDiscoveryListener();
			inboundQueue = new MessageQueue();
			outboundQueue = new MessageQueue();
			connectionManager = new ConnectionManager(port, nodeConnectionListener);
			functionManager = new FunctionManager(nodeFunctionListener);
			directory = new Directory();
			discoveryManager = new DiscoveryManager(nodeDiscoveryListener, nodeId, connectionManager.getAddress());
			correlationManager = new CorrelationManager();
			knownAddresses = new ArrayList<Address>();
			nodeControlLoop = new NodeControlLoop();
			nodeControlLoop.setName("Firebus Node " + nodeId);
			nodeControlLoop.start();			
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

	protected void controlLoop()
	{
		try
		{
			//discoveryManager.sendDiscoveryRequest();
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
	
	protected void resolveKnownAddresses()
	{
		for(int i = 0; i < knownAddresses.size(); i++)
		{
			try 
			{
				Connection connection = connectionManager.createConnection(knownAddresses.get(i));
				if(connection != null)
				{
					Message msg = new Message(0, nodeId, 0, Message.MSGTYPE_CONNECT, 0, null, getNodeStateString().getBytes());
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
				Message msg = new Message(ni.getNodeId(), nodeId, 0, Message.MSGTYPE_CONNECT, 0, null, getNodeStateString().getBytes());
				msg.setConnection(c);
				msg.setRepeatsLeft(0);
				outboundQueue.addMessage(msg);
			}
		}
	}
	
	protected void processNextInboundMessage()
	{
		if(verbose == 2)
			System.out.println("Processing Inbound Message");
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
						System.out.println("duplicate connection");
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
						break;
					case Message.MSGTYPE_QUERYNODE:
						advertiseTo(msg.getOriginatorId());
						break;
					case Message.MSGTYPE_FINDSERVICE:
						if(functionManager.hasFunction(msg.getSubject()))
							advertiseTo(msg.getOriginatorId());
						break;
					case Message.MSGTYPE_REQUESTSERVICE:
						correlationManager.addMessage(msg.getCorrelation(), msg);
						functionManager.requestService(msg.getSubject(), msg.getPayload(), msg.getCorrelation());
						break;
					case Message.MSGTYPE_SERVICERESPONSE:
						correlationManager.addMessage(msg.getCorrelation(), msg);
						break;
					case Message.MSGTYPE_PUBLISH:
						functionManager.consume(msg.getSubject(), msg.getPayload());
				}
			}
			
			if(msg.getDestinationId() == 0  ||  msg.getDestinationId() != nodeId)
			{
				if(msg.getRepeatCount() > 0)
					outboundQueue.addMessage(msg.repeat(nodeId));
			}
			
			//System.out.println("****Inbound****************\r\n" + msg);
		}

		inboundQueue.deleteNextMessage();
		if(verbose == 2)
			System.out.println("Finished Processing Inbound Message");

	}

	protected void processNextOutboundMessage()
	{
		if(verbose == 2)
			System.out.println("Processing Outbound Message");

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
		
		//System.out.println("****Oubound**************\r\n" + msg);
		outboundQueue.deleteNextMessage();
		if(verbose == 2)
			System.out.println("Finished Processing Outbound Message");

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
		Message msg = new Message(destinationNodeId, nodeId, 0, Message.MSGTYPE_NODESTATE, 0, null, getNodeStateString().getBytes());
		outboundQueue.addMessage(msg);
	}
	
	public void registerServiceProvider(String serviceName, ServiceProvider serviceProvider)
	{
		functionManager.addFunction(serviceName, serviceProvider);
		advertiseTo(0);
	}
	
	public void registerConsumer(String dataName, Consumer consumer)
	{
		functionManager.addFunction(dataName, consumer);
	}
		
	public byte[] requestService(String serviceName, byte[] payload)
	{
		if(verbose == 2)
			System.out.println("Requesting Service");

		NodeInformation ni = directory.findServiceProvider(serviceName);
		if(ni == null)
		{
			int waitCount = 0;
			Message msg = new Message(0, nodeId, 0, Message.MSGTYPE_FINDSERVICE, 0, serviceName, null);
			outboundQueue.addMessage(msg);
			while((ni = directory.findServiceProvider(serviceName)) == null  &&  waitCount < 200)
			{
				try {Thread.sleep(10);} catch (Exception e) {}
				waitCount++;
			}
		}
		
		int correlation = correlationManager.getNextCorrelation();
		Message msg = new Message(ni.getNodeId(), nodeId, 0, Message.MSGTYPE_REQUESTSERVICE, correlation, serviceName, payload);
		outboundQueue.addMessage(msg);
		
		while(!correlationManager.hasMessage(correlation))
			try {Thread.sleep(10);} catch (Exception e) {}

		byte[] returnPayload = correlationManager.getMessage(correlation).getPayload();
		correlationManager.removeCorrelation(correlation);
		
		if(verbose == 2)
			System.out.println("Returning Service Response");

		return returnPayload;
	}		

	public void publish(String dataname, byte[] payload)
	{
		Message msg = new Message(0, nodeId, 0, Message.MSGTYPE_PUBLISH, 0, dataname, payload);
		outboundQueue.addMessage(msg);
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
