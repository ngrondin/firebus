package com.nic.firebus;

import java.io.IOException;
import java.util.Random;

public class Node extends Thread implements ConnectionListener, FunctionListener
{
	protected int nodeId;
	protected boolean quit;
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
		try
		{
			Connection c = connectionManager.createConnection(address);
			Message msg = new Message(0, nodeId, 0, Message.MSGTYPE_QUERYNODE, 0, null, null);
			c.sendMessage(msg);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void messageReceived(Message m, Connection c) 
	{
		inboundQueue.addMessage(m);
	}

	public void connectionClosed(Connection c) 
	{
		NodeInformation ni = directory.getNodeByConnection(c);
		if(ni != null)
			ni.setConnection(null);
		connectionManager.dropConnection(c);
	}

	public void functionCallback(int correlation, byte[] payload) 
	{
		Message originalMsg = correlationManager.getMessage(correlation);
		correlationManager.removeCorrelation(correlation);
		Message msg = new Message(originalMsg.getOriginator(), nodeId, 0, Message.MSGTYPE_SERVICERESPONSE, correlation, originalMsg.getSubject(), payload);
		outboundQueue.addMessage(msg);
	}


	protected void processNextInboundMessage()
	{
		Message msg = inboundQueue.getNextMessage();
		msg.decode();
		
		if(msg.getOriginator() != nodeId)
		{
			NodeInformation orig = directory.getOrCreateNode(msg.getOriginator());
			if(msg.getRepeater() != 0)
			{
				NodeInformation rpt = directory.getOrCreateNode(msg.getRepeater());
				rpt.setConnection(msg.getConnection());
				orig.addRepeater(msg.getRepeater());
			}
			else
			{
				orig.setConnection(msg.getConnection());
			}
			
			if(msg.getDestination() == 0  ||  msg.getDestination() == nodeId)
			{
				switch(msg.getType())
				{
					case Message.MSGTYPE_ADVERTISE:
						directory.processAdvertisementMessage(msg.getPayload());
						break;
					case Message.MSGTYPE_QUERYNODE:
						advertiseTo(msg.getOriginator(), null);
						break;
					case Message.MSGTYPE_FIND:
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
				outboundQueue.addMessage(msg.repeat(nodeId));
			}
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
			NodeInformation ni = directory.getOrCreateNode(dest);
			c = connectionManager.obtainConnectionForNode(ni);
			if(c == null)
			{
				int rpt = ni.getRandomRepeater();
				if(rpt != 0)
				{
					ni = directory.getNode(rpt);
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
			try
			{
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
