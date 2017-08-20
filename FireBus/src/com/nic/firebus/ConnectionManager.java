package com.nic.firebus;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.nic.firebus.exceptions.ConnectionException;
import com.nic.firebus.information.NodeInformation;
import com.nic.firebus.interfaces.ConnectionListener;


public class ConnectionManager extends Thread
{
	private Logger logger = Logger.getLogger(ConnectionManager.class.getName());
	protected int port;
	protected boolean quit;
	protected String netName;
	protected String key;
	protected ServerSocket server;
	protected NodeCore nodeCore;
	protected ArrayList<Connection> connections;
	
	public ConnectionManager(NodeCore nc, String n, String k) throws IOException
	{
		initialise(nc, 0, n, k);
	}
	
	public ConnectionManager(int p, NodeCore nc, String n, String k) throws IOException
	{
		initialise(nc, p, n, k);
	}
	
	protected void initialise(NodeCore nc, int p, String n, String k) throws IOException 
	{
		connections = new ArrayList<Connection>();
		port = p;
		quit = false;
		netName = n;
		key = k;
		nodeCore = nc;
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
					logger.info("Accepted New Connection");

					Connection connection = new Connection(socket, nodeCore, netName, key);
					connections.add(connection);
				}
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				logger.severe(e.getMessage());
			}
		}
	}
	
	public Connection createConnection(Address a) throws IOException, ConnectionException
	{
		logger.fine("Creating New Connection");
		Connection c = new Connection(a, nodeCore, netName, key, port);
		connections.add(c);
		logger.info("Created New Connection");
		return c;
	}
	
	public Connection obtainConnectionForNode(NodeInformation ni) 
	{
		logger.fine("Obtaining Connection for Node");
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
					break;
				} 
				catch (Exception e) 
				{
					logger.fine(e.getMessage());
				}
			}
			if(c == null)
			{
				ni.setUnconnectable();
				logger.fine("Setting Node Information as Unconnectable ");
			}
		}
		else
		{
			logger.fine("Connection retreived");
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
