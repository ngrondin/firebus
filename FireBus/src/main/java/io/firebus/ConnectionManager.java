package io.firebus;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

import javax.crypto.SecretKey;

import io.firebus.information.KnownAddressInformation;
import io.firebus.information.NodeInformation;
import io.firebus.interfaces.ConnectionListener;


public class ConnectionManager extends Thread implements ConnectionListener
{
	private Logger logger = Logger.getLogger("io.firebus");
	protected int nodeId;
	protected String networkName;
	protected SecretKey secretKey;
	protected boolean quit;
	protected NodeCore nodeCore;
	protected ConnectionServer connectionServer;
	protected ArrayList<Connection> connections;
	protected ArrayList<KnownAddressInformation> knownAddresses;
	protected int minimumConnectedNodeCount;
	protected int maxConnectionLoad;
	protected Random rnd;

	
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
		knownAddresses = new ArrayList<KnownAddressInformation>();
		nodeCore = nc;
		nodeId = nid;
		networkName = n;
		secretKey = k;
		connectionServer = new ConnectionServer(this, p);
		rnd = new Random();
		quit = false;
		minimumConnectedNodeCount = 3;
		maxConnectionLoad = 50000;
		setName("fbConnMgr");
		start();		
	}

	/********* Getters ***********/
	
	public int getPort()
	{
		return connectionServer.getPort();
	}
	
	public int getConnectedNodeCount()
	{
		ArrayList<Integer> connectedNodeIds = new ArrayList<Integer>();
		for(int i = 0; i < connections.size(); i++)
			if(!connectedNodeIds.contains(connections.get(i).getRemoteNodeId()))
					connectedNodeIds.add(connections.get(i).getRemoteNodeId());
		return connectedNodeIds.size();
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
	
	
	/******* Management Loop ************/
	
	public void run()
	{
		while(!quit)
		{
			try 
			{
				logger.finest("Maintaining connection counts");
				
				for(int i = 0; i < knownAddresses.size(); i++)
				{
					KnownAddressInformation kai = knownAddresses.get(i);
					if(kai.shouldRemove())
					{
						knownAddresses.remove(i);
						i--;
					}
					else
					{
						boolean hasConnection = false;
						for(int j = 0; j < connections.size(); j++)
							if(connections.get(j).remoteAddressEquals(kai.getAddress()))
								hasConnection = true;
						
						if(!hasConnection && kai.isDueToTry())
						{
							logger.fine("Creating new connection from known address (try " + kai.tries() + ")");
							createConnection(kai.getAddress());
						}						
					}
				}
				
				int connectedNodeCount = getConnectedNodeCount();
				for(int i = 0; i < nodeCore.getDirectory().getNodeCount()  &&  connectedNodeCount < minimumConnectedNodeCount; i++)
				{
					NodeInformation ni = nodeCore.getDirectory().getNode(i);
					if(ni.getNodeId() != nodeId  &&  ni.getAddressCount() > 0)
					{
						boolean hasConnection = false;
						for(int j = 0; j < connections.size(); j++)
							for(int k = 0; k < ni.getAddressCount(); k++)
								if(connections.get(j).remoteAddressEquals(ni.getAddress(k)))
									hasConnection = true;
						if(!hasConnection)
						{
							logger.fine("Creating new connection to maintain minimum node connectivity");
							createConnection(ni.getAddress(0));
							connectedNodeCount++;
						}
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
	
	/********* Event Handlers **********/
	
	public void socketReceived(Socket socket, int port)
	{
		try
		{
			Connection c = new Connection(socket, networkName, secretKey, nodeId, port, this);
			connections.add(c);
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
		}
	}


	public synchronized void connectionCreated(Connection c)
	{
		nodeCore.getDirectory().processDiscoveredNode(c.getRemoteNodeId(),  c.getRemoteAddress());
	}
	
	public synchronized void connectionFailed(Connection c)
	{
		Address a = c.getRemoteAddress();
		if(a != null)
		{
			logger.fine("Connection " + c.getId() + " failed.");
			NodeInformation ni = nodeCore.getDirectory().getNodeByAddress(a); 
			if(ni != null) {
				logger.finer("Removing address " + a + " from node");
				ni.removeAddress(a); 
			}
			for(KnownAddressInformation kai: knownAddresses) {
				if(kai.getAddress().equals(a)) {
					kai.connectionFailed();
					if(c.remoteNodeId == c.localNodeId)
						kai.setAsSelf();
				}
			}
		}
	}
	
	public void messageReceived(Message m, Connection c)
	{
		int originatorId = m.getOriginatorId();
		int connectedId = c.getRemoteNodeId();
		if(originatorId != nodeId)
		{
			logger.finest("Received message from node " + c.getRemoteNodeId());
			if(connectedId != originatorId)
			{
				NodeInformation originatorNode = nodeCore.getDirectory().getOrCreateNodeInformation(originatorId);
				originatorNode.addRepeater(connectedId);
			}
			nodeCore.enqueue(m);
		}
		else
		{
			logger.finest("Blocked message from self");
		}
	}

	public synchronized void connectionClosed(Connection c)
	{
		logger.fine("Connection " + c.getId() + " Closed");
		connections.remove(c);
	}
	
	
	/********* Execution Services **********/
	
	public void addKnownNodeAddress(String a, int p)
	{
		boolean exists = false;
		Address address = new Address(a, p);
		for(KnownAddressInformation kai: knownAddresses) {
			if(kai.getAddress().equals(address))
				exists = true;
		}
		if(!exists) {
			KnownAddressInformation kai = new KnownAddressInformation(address);
			knownAddresses.add(kai);			
		}
	}	
	
	public void sendMessage(Message msg)
	{
		int destinationNodeId = msg.getDestinationId();
		Connection c = null;
		if(destinationNodeId != 0)
		{
			NodeInformation ni = nodeCore.getDirectory().getNodeById(destinationNodeId);
			if(ni != null)
				c = getOrCreateConnectionForNode(ni);
		}

		if(c != null)
		{
			c.sendMessage(msg);
		}
		else
		{
			broadcastToAllConnections(msg);
		}
	}
	
	protected Connection getOrCreateConnectionForNode(NodeInformation ni) 
	{
		Connection c = getExistingConnectionForNode(ni);
		if(c == null)
		{
			if(ni.getAddressCount() > 0)
				for(int i = 0; i < ni.getAddressCount() && c == null; i++)
					c = createConnection(ni.getAddress(i));
				
			if(c == null) 
			{
				int rpt = ni.getRandomRepeater();
				if(rpt != 0)
				{
					ni = nodeCore.getDirectory().getNodeById(rpt);
					c = getOrCreateConnectionForNode(ni);
				}
			}
		}
		return c;
	}
	
	protected Connection getExistingConnectionForNode(NodeInformation ni)
	{
		ArrayList<Connection> cons = new ArrayList<Connection>();
		for(int i = 0; i < connections.size(); i++)
			if(connections.get(i).getRemoteNodeId() == ni.getNodeId())
					cons.add(connections.get(i));
					
		if(cons.size() > 0)
			return cons.get(rnd.nextInt(cons.size()));
		else
			return null;
	}
	
	
	protected Connection createConnection(Address a) 
	{
		logger.fine("Creating new connection");
		Connection c = new Connection(a, networkName, secretKey, nodeId, connectionServer.getPort(), this);
		connections.add(c);
		return c;
	}
	
	
	protected void broadcastToAllConnections(Message msg)
	{
		logger.finer("Broadcasting message to all connections");
		for(int i = 0; i < connections.size(); i++)
		{
			Connection c = connections.get(i);
			if(c.getRemoteNodeId() != msg.getOriginatorId())
				c.sendMessage(msg);
		}
	}
	
	public void close()
	{
		try
		{
			quit = true;
			connectionServer.close();
			for(int i = connections.size() - 1; i >= 0; i--)
				connections.get(i).close();
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
		}
	}
	
	

	/******** Printers *********/
	
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
