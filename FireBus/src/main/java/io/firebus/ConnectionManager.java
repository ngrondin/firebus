package io.firebus;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import javax.crypto.SecretKey;

import io.firebus.information.KnownAddressInformation;
import io.firebus.information.NodeInformation;
import io.firebus.interfaces.ConnectionListener;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;


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
	
	public boolean hasConnectionForAddress(Address a)
	{
		for(Connection connection: connections)
			if(connection.remoteAddressEquals(a))
				return true;
		return false;
	}
	
	public boolean hasConnectionForNode(NodeInformation ni) 
	{
		for(Connection connection: connections)
			if(connection.getRemoteNodeId() == ni.getNodeId())
				return true;
		return false;
	}
	
	public int getConnectedNodeCount()
	{
		ArrayList<Integer> connectedNodeIds = new ArrayList<Integer>();
		for(Connection connection: connections)
			if(!connectedNodeIds.contains(connection.getRemoteNodeId()))
					connectedNodeIds.add(connection.getRemoteNodeId());
		return connectedNodeIds.size();
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
						if(!hasConnectionForAddress(kai.getAddress()) && kai.isDueToTry())
						{
							logger.info("Creating new connection from known address (try " + kai.tries() + ")");
							createConnection(kai.getAddress());
						}						
					}
				}
				
				int connectedNodeCount = getConnectedNodeCount();
				for(int i = 0; i < nodeCore.getDirectory().getNodeCount()  &&  connectedNodeCount < minimumConnectedNodeCount; i++)
				{
					NodeInformation ni = nodeCore.getDirectory().getNode(i);
					if(ni.getNodeId() != nodeId  &&  ni.getAddressCount() > 0 && !hasConnectionForNode(ni))
					{
						logger.info("Creating new connection to maintain minimum node connectivity");
						createConnection(ni.getAddress(0));
						connectedNodeCount++;
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
		Connection connection = getExistingConnectionForNode(ni);
		if(connection == null)
		{
			for(int i = 0; i < ni.getAddressCount() && connection == null; i++) {
				logger.info("Creating a new connection for a direct message");
				connection = createConnection(ni.getAddress(i));
			}
			
			if(connection == null) 
			{
				for(int i = 0; i < ni.getRepeaterCount() && connection == null; i++) {
					NodeInformation repeater = nodeCore.getDirectory().getNodeById(ni.getRepeater(i));
					for(int j = 0; j < repeater.getAddressCount() && connection == null; j++) {
						logger.info("Creating a new connection for a repeater message");
						connection = createConnection(repeater.getAddress(j));
					}	
				}
			}
		}
		return connection;
	}
	
	protected Connection getExistingConnectionForNode(NodeInformation ni)
	{
		ArrayList<Connection> readyConnections = new ArrayList<Connection>();
		ArrayList<Connection> allConnections = new ArrayList<Connection>();
		for(Connection connection: connections) {
			if(connection.getRemoteNodeId() == ni.getNodeId()) {
				allConnections.add(connection);
				if(connection.isReady())
					readyConnections.add(connection);
			}
		}
					
		if(readyConnections.size() > 0)
			return readyConnections.get(rnd.nextInt(readyConnections.size()));
		else if(allConnections.size() > 0)
			return allConnections.get(rnd.nextInt(allConnections.size()));
		else
			return null;
	}
	
	
	protected Connection createConnection(Address a) 
	{
		Connection c = new Connection(a, networkName, secretKey, nodeId, connectionServer.getPort(), this);
		connections.add(c);
		return c;
	}
	
	
	protected void broadcastToAllConnections(Message msg)
	{
		logger.finer("Broadcasting message to all connections");
		List<Integer> sentToNodeIds = new ArrayList<Integer>();
		for(int i = 0; i < connections.size(); i++)
		{
			Connection c = connections.get(i);
			int remoteNodeId = c.getRemoteNodeId();
			if(remoteNodeId != msg.getOriginatorId() && !sentToNodeIds.contains(remoteNodeId)) {
				c.sendMessage(msg);
				sentToNodeIds.add(remoteNodeId);
			}
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
	
	public DataMap getStatus()
	{
		DataMap status = new DataMap();
		DataMap connMap = new DataMap();
		for(Connection connection: connections) {
			connMap.put(String.valueOf(connection.getId()), connection.getStatus());
		}
		status.put("connections", connMap);
		DataList kaList = new DataList();
		for(KnownAddressInformation ka: knownAddresses) {
			kaList.add(ka.toString());
		}
		status.put("knownAddress", kaList);
		return status;
	}


}
