package io.firebus;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.crypto.SecretKey;

import io.firebus.information.KnownAddressInformation;
import io.firebus.information.NodeInformation;
import io.firebus.interfaces.ConnectionListener;
import io.firebus.logging.Logger;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;


public class ConnectionManager extends Thread implements ConnectionListener
{
	protected int nodeId;
	protected String networkName;
	protected SecretKey secretKey;
	protected boolean quit;
	protected NodeCore nodeCore;
	protected ConnectionServer connectionServer;
	protected List<Connection> connections;
	protected Map<Integer, List<Connection>> connectionsForNodeId;
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
		connectionsForNodeId = new HashMap<Integer, List<Connection>>();
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
				Logger.finest("fb.connections.manager.maintaining");		
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
							Logger.finest("fb.connections.manager.creating.knownaddress", new DataMap("try", kai.tries()));		
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
						Logger.finest("fb.connections.manager.creating.minimum");		
						createConnection(ni.getAddress(0));
						connectedNodeCount++;
					}
				}

				sleep(500);
			} 
			catch (Exception e) 
			{
				Logger.severe("fb.connections.manager.maintaining", e);
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
			Logger.severe("fb.connections.manager.received", e);
		}
	}


	public synchronized void connectionCreated(Connection c)
	{
		int nodeId = c.getRemoteNodeId();
		if(!connectionsForNodeId.containsKey(nodeId)) 
			connectionsForNodeId.put(nodeId, new ArrayList<Connection>());
		connectionsForNodeId.get(nodeId).add(c);
		nodeCore.getDirectory().processDiscoveredNode(c.getRemoteNodeId(),  c.getRemoteAddress());
	}
	
	public synchronized void connectionFailed(Connection c)
	{
		Address a = c.getRemoteAddress();
		if(a != null)
		{
			Logger.fine("fb.connections.manager.connfailed", new DataMap("conn", c.getId()));
			NodeInformation ni = nodeCore.getDirectory().getNodeByAddress(a); 
			if(ni != null) {
				Logger.finer("fb.connections.manager.removingaddr", new DataMap("address", a));
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
			Logger.fine("fb.connections.manager.blockedself");
		}
	}

	public synchronized void connectionClosed(Connection c)
	{
		Logger.fine("fb.connections.manager.closed", new DataMap("conn", c.getId()));
		connections.remove(c);
		int nodeId = c.getRemoteNodeId();
		List<Connection> list = connectionsForNodeId.get(nodeId);
		if(list != null)
			list.remove(c);
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
		Connection connection = getReadyConnectionForNodeId(nodeId);
		if(connection == null)
		{
			connection = getAnyConnectionForNodeId(nodeId);
			if(connection == null) 
			{
				NodeInformation ni = nodeCore.getDirectory().getNodeById(nodeId);
				if(ni != null) {
					for(int i = 0; i < ni.getAddressCount() && connection == null; i++) {
						Logger.info("fb.connections.manager.creating.directmessage");
						connection = createConnection(ni.getAddress(i));
					}
					
					if(connection == null) 
					{
						for(int i = 0; i < ni.getRepeaterCount() && connection == null; i++) {
							connection = getReadyConnectionForNodeId(ni.getRepeater(i));
						}
						
						if(connection == null) {
							for(int i = 0; i < ni.getRepeaterCount() && connection == null; i++) {
								NodeInformation repeater = nodeCore.getDirectory().getNodeById(ni.getRepeater(i));
								for(int j = 0; j < repeater.getAddressCount() && connection == null; j++) {
									Logger.fine("fb.connections.manager.creating.repeatermessage");
									connection = createConnection(repeater.getAddress(j));
								}								
							}	
						}
					}
				}
			}
		}
		return connection;
	}
	
	protected Connection getReadyConnectionForNodeId(int nodeId)
	{
		Connection connection = null;
		List<Connection> list = connectionsForNodeId.get(nodeId);
		if(list != null) {
			int size = list.size();
			if(size > 0)
				connection = list.get(rnd.nextInt(size));
		}
		return connection;
	}
	
	protected Connection getAnyConnectionForNodeId(int nodeId)
	{
		for(Connection connection: connections)
			if(connection.getRemoteNodeId() == nodeId)  
				return connection;
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
		Logger.finer("fb.connections.manager.broadcasting");
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
			Logger.severe("fb.connections.manager.closeing", e);
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
