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
	protected ConnectionListener connectionListener;
	protected ArrayList<Connection> connections;
	
	public ConnectionManager(ConnectionListener cl)
	{
		initialise(cl, 1991);
	}
	
	public ConnectionManager(int p, ConnectionListener cl)
	{
		initialise(cl, p);
	}
	
	protected void initialise(ConnectionListener cl, int p)
	{
		connections = new ArrayList<Connection>();
		port = p;
		quit = false;
		connectionListener = cl;
		setName("Firebus Node Connection Manager");
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
		Connection c = new Connection(a, connectionListener);
		connections.add(c);
		return c;
	}
	
	public Connection obtainConnectionForNode(NodeInformation ni)
	{
		Connection c = ni.getConnection();
		if(c == null)
		{
			for(int i = 0; i < ni.getAddressCount(); i++)
			{
				Address a = ni.getAddress(i);
				try 
				{
					c = createConnection(a);
					ni.setConnection(c);
					break;
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
	
	public void removeConnection(Connection c)
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
	
	public String getAddressAdvertisementString(int nodeId)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(nodeId);
		sb.append(",a,");
		sb.append(getLocalAddress().getHostAddress());
		sb.append(",");
		sb.append(getPort());
		sb.append("\r\n");
		return sb.toString();
	}

}
