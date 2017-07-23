package com.nic.firebus;

import java.util.ArrayList;
import java.util.Random;

public class Node extends Thread implements ConnectionListener, FunctionListener
{
	protected int nodeId;
	protected boolean quit;
	protected long lastAddressResolution;
	protected MessageQueue inboundQueue;
	protected MessageQueue outboundQueue;
	protected ConnectionManager connectionManager;
	protected FunctionManager functionManager;
	protected Directory directory;
	protected CorrelationManager correlationManager;
	
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
		connectionManager = new ConnectionManager(port, this);
		functionManager = new FunctionManager(this);
		directory = new Directory();
		correlationManager = new CorrelationManager();
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
		NodeInformation ni = directory.getNodeByAddress(address);
		if(ni == null)
		{
			ni = new NodeInformation(address);
			directory.addNode(ni);
			Message msg = new Message(0, nodeId, 0, Message.MSGTYPE_DISCOVER, 0, null, null);
			msg.setRepeatsLeft(0);
			outboundQueue.addMessage(msg);
		}
	}

	public void messageReceived(Message m, Connection c) 
	{
		inboundQueue.addMessage(m);
	}

	public void connectionClosed(Connection c) 
	{
		connectionManager.removeConnection(c);
	}

	public void functionCallback(int correlation, byte[] payload) 
	{
		Message originalMsg = correlationManager.getMessage(correlation);
		correlationManager.removeCorrelation(correlation);
		Message msg = new Message(originalMsg.getOriginator(), nodeId, 0, Message.MSGTYPE_SERVICERESPONSE, correlation, originalMsg.getSubject(), payload);
		outboundQueue.addMessage(msg);
	}

	protected void resolveAddresses()
	{
		ArrayList<NodeInformation> nodes = directory.getUnresolvedAndUnconnected();
		for(int i = 0; i < nodes.size(); i++)
		{
			NodeInformation ni = nodes.get(i);
			Connection connection = connectionManager.obtainConnectionForNode(ni);
			ni.setConnection(connection);
		}
	}

	protected void processNextInboundMessage()
	{
		Message msg = inboundQueue.getNextMessage();
		msg.decode();
		
		if(msg.getOriginator() != nodeId)
		{
			int originatorId = msg.getOriginator();
			int repeaterId = msg.getRepeater();
			int connectedNodeId = repeaterId == 0 ? originatorId : repeaterId;
			NodeInformation connectedNode = directory.getNodeByConnection(msg.getConnection());
			if(connectedNode == null)
			{
				connectedNode = directory.getOrCreateNodeById(connectedNodeId);
				connectedNode.setConnection(msg.getConnection());
			}
			else if(connectedNode.getNodeId() == 0)
			{
				connectedNode.setNodeId(connectedNodeId);
			}

			if(repeaterId != 0)
			{
				NodeInformation originatorNode = directory.getOrCreateNodeById(originatorId);
				originatorNode.addRepeater(repeaterId);
			}
			
			if(msg.getDestination() == 0  ||  msg.getDestination() == nodeId)
			{
				switch(msg.getType())
				{
					case Message.MSGTYPE_ADVERTISE:
						directory.processAdvertisementMessage(msg.getPayload());
						break;
					case Message.MSGTYPE_DISCOVER:
						advertiseTo(msg.getOriginator(), null);
						break;
					case Message.MSGTYPE_FIND:
						if(functionManager.find(msg.getSubject()) != null)
							advertiseTo(msg.getOriginator(), msg.getSubject());
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
			
			if(msg.getDestination() == 0  ||  msg.getDestination() != nodeId)
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
		
		Connection c = null;
		int dest = msg.getDestination();
		
		if(dest != 0)
		{
			NodeInformation ni = directory.getOrCreateNodeById(dest);
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
	
	protected void advertiseTo(int dest, String functionName)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(connectionManager.getAddressAdvertisementString());
		sb.append(functionManager.getFunctionAdvertisementString(functionName));
		Message msg = new Message(dest, nodeId, 0, Message.MSGTYPE_ADVERTISE, 0, null, sb.toString().getBytes());
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
				if(directory.getUnresolvedAndUnconnectedCount() > 0)
				{
					resolveAddresses();
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
					sleep(10);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

}
