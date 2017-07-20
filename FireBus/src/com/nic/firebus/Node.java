package com.nic.firebus;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;

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
		nodeId = 0;
		quit = false;
		inboundQueue = new MessageQueue();
		outboundQueue = new MessageQueue();
		connectionManager = new ConnectionManager(1991, this);
		directory = new Directory();
		serviceProviders = new HashMap<String, ServiceProvider>();
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
		
		if(msg.getType() == Message.MSGTYPE_ADVERTISE)
		{
			processAdvertisement(msg);
		}
	}
	
	protected void processAdvertisement(Message msg)
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
					String address = parts[2];
					int port = Integer.parseInt(parts[3]);
					ni.setInetAddress(InetAddress.getByName(address), port);
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
	
	protected Connection obtainConnectionForNode(NodeInformation ni)
	{
		Connection c = ni.getConnection();
		if(c == null)
		{
			InetAddress a = ni.getAddress();
			int p = ni.getPort();
			if(a != null)
			{
				try 
				{
					c = connectionManager.createConnection(a, p);
					ni.setConnection(c);
				} 
				catch (IOException e) 
				{
				}
			}
		}
		return c;
	}
	
	protected void processNextOutboundMessage()
	{
		Message msg = outboundQueue.getNextMessage();
		
		int dest = msg.getDestination();
		NodeInformation ni = directory.getOrCreateNode(dest);
		Connection c = obtainConnectionForNode(ni);
		if(c == null)
		{
			int rpt = ni.getRandomRepeater();
			if(rpt != 0)
			{
				ni = directory.getNode(rpt);
				c = obtainConnectionForNode(ni);
			}
		}
		if(c == null)
		{
			//TODO: Send to other nodes for repitition
		}
		if(c != null)
		{
			msg.encode();
			c.sendMessage(msg);
		}
		
	}
	
	public void registerServiceProvider(String serviceName, ServiceProvider serviceProvider)
	{
		serviceProviders.put(serviceName, serviceProvider);
		advertise();
	}
	
	public void advertise()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(nodeId);
		sb.append(",a,");
		sb.append(connectionManager.getAddress());
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
		Message msg = new Message(Message.MSGTYPE_ADVERTISE, nodeId, 0, 0, null, sb.toString().getBytes());
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
