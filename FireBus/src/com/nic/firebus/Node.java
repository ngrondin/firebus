package com.nic.firebus;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class Node extends Thread implements ConnectionListener
{
	protected int nodeId;
	protected boolean quit;
	protected MessageQueue inboundQueue;
	protected MessageQueue outboundQueue;
	protected ConnectionManager connectionManager;
	protected Directory directory;
	protected HashMap<String, ServiceProvider> serviceProviders;
	
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
		directory = new Directory();
		serviceProviders = new HashMap<String, ServiceProvider>();		
		setName("Firebus Node " + nodeId);
		start();
	}
	
	public int getNodeId()
	{
		return nodeId;
	}
	
	public void addKnownNodeAddress(InetAddress a, int p)
	{
		Address address = new Address(a, p);
		try
		{
			Connection c = connectionManager.createConnection(address);
			Message msg = new Message(0, nodeId, 0, Message.MSGTYPE_QUERYNODE, null, null);
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
						processInboundAdvertisement(msg);
						break;
					case Message.MSGTYPE_QUERYNODE:
						advertiseTo(msg.getOriginator());
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
	

	protected void processInboundAdvertisement(Message msg)
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(msg.getPayload())));
		String line;
		try 
		{
			while((line = br.readLine()) != null)
			{
				String[] parts = line.split(",");
				int id = Integer.parseInt(parts[0]);
				NodeInformation ni = directory.getOrCreateNode(id);
				if(parts[1].equals("a"))
				{
					try
					{
						ni.setInetAddress(new Address(InetAddress.getByName(parts[2]), Integer.parseInt(parts[3])));
					}
					catch(Exception e)	{	}
				}
				else if(parts[1].equals("s"))
				{
					String serviceName = parts[2];
					ServiceInformation si = ni.getService(serviceName);
					if(si == null)
						si = new ServiceInformation(serviceName);
					ni.addService(si);
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
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
	
	
	public void registerServiceProvider(String serviceName, ServiceProvider serviceProvider)
	{
		serviceProviders.put(serviceName, serviceProvider);
		advertise();
	}
	
	protected void advertise()
	{
		advertiseTo(0);
	}
	
	protected void advertiseTo(int dest)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(nodeId);
		sb.append(",a,");
		sb.append(connectionManager.getLocalAddress().toString());
		sb.append(",");
		sb.append(connectionManager.getPort());
		sb.append("\r\n");

		Iterator<String> it = serviceProviders.keySet().iterator();
		while(it.hasNext())
		{
			String serviceName = it.next();
			sb.append(nodeId);
			sb.append(",s,");
			sb.append(serviceName);
			sb.append("\r\n");
		}
		Message msg = new Message(dest, nodeId, 0, Message.MSGTYPE_ADVERTISE, null, sb.toString().getBytes());
		outboundQueue.addMessage(msg);
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
