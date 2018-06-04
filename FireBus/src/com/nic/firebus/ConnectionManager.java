package com.nic.firebus;

import java.io.IOException;
import java.net.InetAddress;
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
	//protected int port;
	protected boolean quit;
	//protected ServerSocket server;
	protected NodeCore nodeCore;
	protected ConnectionServer connectionServer;
	protected ArrayList<Connection> connections;
	protected ArrayList<Address> knownAddresses;

	
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
		knownAddresses = new ArrayList<Address>();
		nodeCore = nc;
		nodeId = nid;
		networkName = n;
		secretKey = k;
		connectionServer = new ConnectionServer(this, p);
		quit = false;
		setName("fbConnMgr");
		start();		
	}

	public void close()
	{
		try
		{
			quit = true;
			connectionServer.close();
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
				int i1 = 0;
				int i2 = 0;
				logger.finest("Maintaining connection counts");
				while(connections.size() < 3  &&  !(i1 >= knownAddresses.size()  &&  i2 >= nodeCore.getDirectory().getNodeCount()))
				{
					if(i1 < knownAddresses.size())
					{
						Address a = knownAddresses.get(i1);
						if(getConnectionByAddress(a) == null)
						{
							try
							{
								createConnection(a);
							}
							catch(Exception e)
							{
								logger.severe(e.getMessage());
							}
						}
						i1++;
					}
					else if(i2 < nodeCore.getDirectory().getNodeCount())
					{
						NodeInformation ni = nodeCore.getDirectory().getNode(i2);
						if(getConnectionByNodeId(ni.getNodeId()) == null  &&  ni.getAddressCount() > 0  &&  !ni.isUnconnectable())
							obtainConnectionForNode(ni);
						i2++;
					}
				}
				sleep(500);
			} 
			catch (Exception e) 
			{
				logger.severe(e.getMessage());
			}
		}
	}
	
	public void addKnownNodeAddress(String a, int p)
	{
		Address address = new Address(a, p);
		knownAddresses.add(address);
		synchronized(this)
		{
			this.notify();
		}
	}
	
	public void socketReceived(Socket socket, int port)
	{
		try
		{
			Connection connection = new Connection(socket, networkName, secretKey, nodeId, port, this);
			connections.add(connection);
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
		}
	}


	public void connectionCreated(Connection c)
	{
		nodeCore.getDirectory().processDiscoveredNode(c.getRemoteNodeId(),  c.getRemoteAddress());
	}
	
	public void messageReceived(Message m, Connection c)
	{
		int originatorId = m.getOriginatorId();
		int connectedId = c.getRemoteNodeId();
		if(originatorId != nodeId)
		{
			logger.fine("Received message from node " + c.getRemoteNodeId());
			if(connectedId != originatorId)
			{
				NodeInformation originatorNode = nodeCore.getDirectory().getOrCreateNodeInformation(originatorId);
				originatorNode.addRepeater(connectedId);
			}
			nodeCore.forkThenRoute(m);
		}
		else
		{
			logger.fine("Blocked message from self");
		}
	}

	public void connectionClosed(Connection c)
	{
		logger.info("Connection " + c.getId() + " Closed");
		removeConnection(c);
	}
	
	public Connection createConnection(Address a) throws IOException, ConnectionException
	{
		logger.fine("Creating New Connection");
		Connection c = new Connection(a, networkName, secretKey, nodeId, connectionServer.getPort(), this);
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
			logger.fine("Connection " + c.getId() + " retreived");
		}
		
		return c;
	}
	
	public int getPort()
	{
		return connectionServer.getPort();
	}
	
	/*
	public int getConnectionCount()
	{
		return connections.size();
	}
*/
	
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
	
	public void sendMessage(Message msg)
	{
		int destinationNodeId = msg.getDestinationId();
		Connection c = null;
		if(destinationNodeId != 0)
		{
			NodeInformation ni = nodeCore.getDirectory().getNodeById(destinationNodeId);
			if(ni != null)
			{
				c = obtainConnectionForNode(ni);
				if(c == null)
				{
					int rpt = ni.getRandomRepeater();
					if(rpt != 0)
					{
						ni = nodeCore.getDirectory().getNodeById(rpt);
						c = obtainConnectionForNode(ni);
					}
				}							
			}
		}

		if(c == null)
			broadcastToAllConnections(msg);
		else
			c.sendMessage(msg);

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
			return new Address(InetAddress.getLocalHost().getHostAddress(), connectionServer.getPort());
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
