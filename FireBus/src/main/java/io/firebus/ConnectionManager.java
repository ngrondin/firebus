package io.firebus;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.crypto.SecretKey;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.information.KnownAddressInformation;
import io.firebus.information.NodeInformation;
import io.firebus.interfaces.ConnectionListener;
import io.firebus.logging.Logger;


public class ConnectionManager extends Thread implements ConnectionListener
{
	protected int nodeId;
	protected String networkName;
	protected SecretKey secretKey;
	protected boolean quit;
	protected NodeCore nodeCore;
	protected ConnectionServer connectionServer;
	protected List<Connection> connections;
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
	
	public boolean hasConnectionForNode(int nodeId) 
	{
		for(Connection connection: connections)
			if(connection.getRemoteNodeId() == nodeId)
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
				Logger.finest("fb.connections.maintaining");		
				for(int i = 0; i < knownAddresses.size(); i++)
				{
					KnownAddressInformation kai = knownAddresses.get(i);
					if(kai.shouldRemove())
					{
						Logger.info("fb.connections.maintaining.removeknownaddress", new DataMap("address", kai.getAddress().toString()));
						knownAddresses.remove(i);
						i--;
					}
					else
					{
						if(!hasConnectionForAddress(kai.getAddress()) && kai.isDueToTry())
						{
							createConnection(kai.getAddress());
						}						
					}
				}
				
				int connectedNodeCount = getConnectedNodeCount();
				for(int i = 0; i < nodeCore.getDirectory().getNodeCount()  &&  connectedNodeCount < minimumConnectedNodeCount; i++)
				{
					NodeInformation ni = nodeCore.getDirectory().getNode(i);
					if(ni.getNodeId() != nodeId  &&  ni.getAddressCount() > 0 && !hasConnectionForNode(ni.getNodeId()))
					{
						Logger.finest("fb.connections.creating.minimum");		
						createConnection(ni.getAddress(0));
						connectedNodeCount++;
					}
				}
				
				synchronized(this) {
					for(Connection c : connections) {
						if(!c.isHealthy() && !c.isClosing()) {
							c.close();						
						}
					}					
				}

				sleep(500);
			} 
			catch (Exception e) 
			{
				Logger.severe("fb.connections.maintaining", e);
			}
		}
	}
	
	/********* Event Handlers **********/
	
	public synchronized void socketReceived(Socket socket, int port)
	{
		try
		{
			Connection c = new Connection(socket, networkName, secretKey, nodeId, port, this);
			connections.add(c);
		}
		catch(Exception e)
		{
			Logger.severe("fb.connections.received", e);
		}
	}


	public synchronized void connectionCreated(Connection c)
	{
		nodeCore.getDirectory().processDiscoveredNode(c.getRemoteNodeId(),  c.getRemoteAddress());
		Address a = c.getRemoteAddress();
		if(a != null) {
			for(KnownAddressInformation kai: knownAddresses) {
				if(kai.getAddress().equals(a)) {
					kai.connectionSucceeded();
				}
			}			
		}
	}
	
	public synchronized void connectionFailed(Connection c)
	{
		Address a = c.getRemoteAddress();
		if(a != null)
		{
			//Logger.warning("fb.connections.connfailed", new DataMap("conn", c.getId()));
			NodeInformation ni = nodeCore.getDirectory().getNodeByAddress(a); 
			if(ni != null) {
				//Logger.warning("fb.connections.removingaddr", new DataMap("address", a));
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
			if(connectedId != originatorId)
			{
				NodeInformation originatorNode = nodeCore.getDirectory().getOrCreateNodeInformation(originatorId);
				originatorNode.addRepeater(connectedId);
			}
			nodeCore.enqueue(m);
		}
		else
		{
			Logger.fine("fb.connections.blockedself");
		}
	}

	public synchronized void connectionClosed(Connection c)
	{
		//Logger.info("fb.connections.closed", new DataMap("conn", c.getId()));
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
			c = getOrCreateConnectionForNode(destinationNodeId);

		if(c != null)
			c.sendMessage(msg);
		else
			broadcastToAllConnections(msg);
	}
	
	protected Connection getOrCreateConnectionForNode(int nodeId) 
	{
		Connection connection = getActiveConnectionForNodeId(nodeId);
		if(connection != null) return connection;
		
		connection = getAnyHealthyConnectionForNodeId(nodeId);
		if(connection != null) return connection; 
		
		NodeInformation ni = nodeCore.getDirectory().getNodeById(nodeId);
		if(ni == null) return null;
		
		for(int i = 0; i < ni.getAddressCount() && connection == null; i++) {
			Logger.info("fb.connections.creating.directmessage");
			connection = createConnection(ni.getAddress(i));
		}
		if(connection != null) return connection;
		
		for(int i = 0; i < ni.getRepeaterCount() && connection == null; i++) {
			connection = getActiveConnectionForNodeId(ni.getRepeater(i));
		}
		if(connection != null) return connection;
		
		for(int i = 0; i < ni.getRepeaterCount() && connection == null; i++) {
			NodeInformation repeater = nodeCore.getDirectory().getNodeById(ni.getRepeater(i));
			for(int j = 0; j < repeater.getAddressCount() && connection == null; j++) {
				Logger.fine("fb.connections.creating.repeatermessage");
				connection = createConnection(repeater.getAddress(j));
			}								
		}	
		return connection;
	}
	
	protected Connection getActiveConnectionForNodeId(int nodeId)
	{
		for(Connection connection: connections)
			if(connection.getRemoteNodeId() == nodeId && connection.isActive())  
				return connection;
		return null;
	}
	
	protected Connection getAnyHealthyConnectionForNodeId(int nodeId)
	{
		for(Connection connection: connections)
			if(connection.getRemoteNodeId() == nodeId && connection.isHealthy())  
				return connection;
		return null;
	}
	
	
	protected synchronized Connection createConnection(Address a) 
	{
		Connection c = new Connection(a, networkName, secretKey, nodeId, connectionServer.getPort(), this);
		connections.add(c);
		return c;
	}
	
	
	protected void broadcastToAllConnections(Message msg)
	{
		Logger.finer("fb.connections.broadcasting");
		List<Integer> sentToNodeIds = new ArrayList<Integer>();
		for(Connection c: connections) {
			if(c.isHealthy()) {
				int remoteNodeId = c.getRemoteNodeId();
				if(remoteNodeId != msg.getOriginatorId() && !sentToNodeIds.contains(remoteNodeId)) {
					c.sendMessage(msg);
					sentToNodeIds.add(remoteNodeId);
				}				
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
			Logger.severe("fb.connections.closing", e);
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
