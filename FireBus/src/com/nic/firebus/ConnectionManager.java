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
	protected int verbose;
	protected ServerSocket server;
	protected ConnectionListener connectionListener;
	protected ArrayList<Connection> connections;
	
	public ConnectionManager(ConnectionListener cl) throws IOException
	{
		initialise(cl, 0);
	}
	
	public ConnectionManager(int p, ConnectionListener cl) throws IOException
	{
		initialise(cl, p);
	}
	
	protected void initialise(ConnectionListener cl, int p) throws IOException 
	{
		connections = new ArrayList<Connection>();
		port = p;
		quit = false;
		verbose = 2;
		connectionListener = cl;
		setName("Firebus Connection Manager");
		if(p == 0)
		{
			port = 1990;
			boolean successfulBind = false;
			while(!successfulBind)
			{
				try
				{
					server = new ServerSocket(port);
					successfulBind = true;
				}
				catch(Exception e)	
				{	
					port++;
				}			
			}
		}
		else
		{
			server = new ServerSocket(port);
		}

		start();		
	}

	public void run()
	{
		while(!quit)
		{
			try 
			{
				while(!quit)
				{
					Socket socket = server.accept();
					if(verbose == 2)
						System.out.println("Accepted New Connection");

					Connection connection = new Connection(socket, connectionListener);
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
		if(verbose == 2)
			System.out.println("Creating New Connection");

		Connection c = new Connection(a, connectionListener);
		connections.add(c);
		return c;
	}
	
	public Connection obtainConnectionForNode(NodeInformation ni)
	{
		if(verbose == 2)
			System.out.println("Obtaining Connection for Node");

		Connection c = ni.getConnection();
		if(c == null)
		{
			for(int i = 0; i < ni.getAddressCount()  &&  c == null; i++)
			{
				Address a = ni.getAddress(i);
				try 
				{
					c = createConnection(a);
					ni.setConnection(c);
					if(verbose == 2l)
						System.out.println("Connection Created");
					break;
				} 
				catch (IOException e) 
				{
					if(verbose == 2)
						System.out.println(e.getMessage());
				}
			}
			if(c == null)
			{
				ni.setUnconnectable();
				if(verbose == 2)
					System.out.println("Setting Node Information as Unconnectable ");
			}
		}
		else
		{
			if(verbose == 2l)
				System.out.println("Connection retreived");
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
	
	public void removeConnection(Connection c)
	{
		c.close();
		connections.remove(c);
	}

	public Address getAddress()
	{
		try
		{
			return new Address(InetAddress.getLocalHost().getHostAddress(), port);
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
	
	public int getConnectionCount()
	{
		return connections.size();
	}
	
	public String getAddressStateString(int nodeId)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(nodeId);
		sb.append(",a,");
		sb.append(getAddress().getIPAddress());
		sb.append(",");
		sb.append(getPort());
		sb.append("\r\n");
		return sb.toString();
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < connections.size(); i++)
		{
			sb.append(connections.get(i) + "\r\n");
		}
		return sb.toString();
	}
}
