package com.nic.firebus;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ConnectionManager extends Thread
{
	protected int port;
	protected boolean quit;
	protected ServerSocket server;
	protected Node node;
	protected ArrayList<Connection> connections;
	
	public ConnectionManager(Node n)
	{
		initialise(n, 1991);
	}
	
	public ConnectionManager(int p, Node n)
	{
		initialise(n, p);
	}
	
	protected void initialise(Node n, int p)
	{
		connections = new ArrayList<Connection>();
		port = p;
		quit = false;
		node = n;
		setName("Firebus Node " + n.getNodeId() + " Connection Manager");
		start();		
	}

	public void run()
	{
		while(!quit)
		{
			try 
			{
				server = new ServerSocket(port);
				while(!quit)
				{
					Socket socket = server.accept();
					Connection connection = new Connection(socket, node);
					connections.add(connection);
				}
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	public Connection createConnection(Address a) throws IOException
	{
		Socket socket = new Socket(a.getInetAddress(), a.getPort());
		Connection c = new Connection(socket, node);
		connections.add(c);
		return c;
	}
	
	public Connection obtainConnectionForNode(NodeInformation ni)
	{
		Connection c = ni.getConnection();
		if(c == null)
		{
			Address a = ni.getAddress();
			if(a != null)
			{
				try 
				{
					c = createConnection(a);
					ni.setConnection(c);
				} 
				catch (IOException e) 
				{
				}
			}
		}
		return c;
	}
	
	public void broadcastToAllConnections(Message msg)
	{
		for(int i = 0; i < connections.size(); i++)
		{
			connections.get(i).sendMessage(msg);
		}
	}
	
	public void dropConnection(Connection c)
	{
		c.close();
		connections.remove(c);
	}

	public InetAddress getLocalAddress()
	{
		try
		{
			return InetAddress.getLocalHost();
		} 
		catch (UnknownHostException e)
		{
			return null;
		}
	}
	
	public int getPort()
	{
		return server.getLocalPort();
	}
}
