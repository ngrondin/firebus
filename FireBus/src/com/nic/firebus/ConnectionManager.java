package com.nic.firebus;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.crypto.SecretKey;

import com.nic.firebus.exceptions.ConnectionException;
import com.nic.firebus.information.NodeInformation;
import com.nic.firebus.interfaces.ConnectionListener;


public class ConnectionManager extends Thread implements ConnectionListener
{
	private Logger logger = Logger.getLogger("com.nic.firebus");
	protected int nodeId;
	protected String networkName;
	protected SecretKey secretKey;
	protected int port;
	protected boolean quit;
	protected ServerSocket server;
	protected NodeCore nodeCore;
	protected ArrayList<Connection> connections;
	
	public ConnectionManager(NodeCore nc, int nid, String n, SecretKey k) throws IOException
	{
		initialise(nc, nid, n, k, 0);
	}
	
	public ConnectionManager(NodeCore nc, int nid, String n, SecretKey k, int p) throws IOException
	{
		initialise(nc, nid, n, k, p);
	}
	
	protected void initialise(NodeCore nc, int nid, String n, SecretKey k, int p) throws IOException 
	{
		connections = new ArrayList<Connection>();
		nodeCore = nc;
		nodeId = nid;
		networkName = n;
		secretKey = k;
		port = p;
		quit = false;
		setName("fbConnMgr");
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

	public void close()
	{
		try
		{
			quit = true;
			server.close();
			for(int i = 0; i < connections.size(); i++)
				connections.get(i).close();
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
		}
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
					Connection connection = new Connection(socket, networkName, secretKey, nodeId, port, this);
					connections.add(connection);
				}
			} 
			catch (Exception e) 
			{
				logger.severe(e.getMessage());
			}
		}
	}
	
	public void connectionCreated(Connection c)
	{
		nodeCore.nodeDiscovered(c.getRemoteNodeId(), c.getRemoteAddress());
	}

	public void messageReceived(Message m, Connection c)
	{
		nodeCore.messageReceived(m, c);
	}

	public void connectionClosed(Connection c)
	{
		logger.info("Connection " + c.getId() + " Closed");
		removeConnection(c);
	}
	
	public Connection createConnection(Address a) throws IOException, ConnectionException
	{
		logger.fine("Creating New Connection");
		Connection c = new Connection(a, networkName, secretKey, nodeId, port, this);
		connections.add(c);
		return c;
	}
	
	public Connection obtainConnectionForNode(NodeInformation ni) 
	{
		logger.fine("Obtaining connection for node " + ni.getNodeId());
		Connection c = getConnectionByNodeId(ni.getNodeId());
		if(c == null)
		{
			for(int i = 0; i < ni.getAddressCount()  &&  c == null; i++)
			{
				Address a = ni.getAddress(i);
				try 
				{
					c = createConnection(a);
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
			logger.fine("Connection retreived " + c.getId());
		}
		
		return c;
	}
	
	public int getPort()
	{
		return server.getLocalPort();
	}
	
	public int getConnectionCount()
	{
		return connections.size();
	}

	
	public Connection getConnectionByNodeId(int id)
	{
		for(int i = 0; i < connections.size(); i++)
			if(connections.get(i).getRemoteNodeId() == id)
				return connections.get(i);
		return null;
	}
	
	public Connection getConnectionByAddress(Address a)
	{
		for(int i = 0; i < connections.size(); i++)
			if(connections.get(i).getRemoteAddress() != null  &&  connections.get(i).getRemoteAddress().equals(a))
				return connections.get(i);
		return null;
		
	}
	
	public void broadcastToAllConnections(Message msg)
	{
		logger.fine("Broadcasting message to all connections");
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
