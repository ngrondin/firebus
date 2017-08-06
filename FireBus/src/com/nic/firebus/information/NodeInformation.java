package com.nic.firebus.information;

import java.util.ArrayList;
import java.util.Random;

import com.nic.firebus.Address;
import com.nic.firebus.Connection;

public class NodeInformation 
{
	protected int nodeId;
	protected Connection connection;
	protected ArrayList<Address> addresses;
	protected ArrayList<Integer> repeaters;
	protected ArrayList<ServiceInformation> services;
	protected long lastSentDiscovery;
	protected long lastUpdated;
	protected boolean unconnectable;
	protected boolean unresponsive;
	//protected int status;
	
	
	/*
	public final static int STATUS_NEW = 0;
	public final static int STATUS_CONNECTED = 1;
	public final static int STATUS_DISCONNECTED = 2;
	public final static int STATUS_DISAPPEARED = 3;
	public final static int STATUS_UNREACHABLE = 4;
	 */
	
	public NodeInformation(int ni)
	{
		nodeId = ni;
		initialise();
	}

	protected void initialise()
	{
		services = new ArrayList<ServiceInformation>();
		repeaters = new ArrayList<Integer>();
		addresses = new ArrayList<Address>();
		unconnectable = false;
		unresponsive = false;
	}

	public void setConnection(Connection c)
	{
		connection = c;
		if(c != null)
			unconnectable = false;
	}
	
	public void setLastDiscoverySentTime(long t)
	{
		lastSentDiscovery = t;
	}
	
	public void setLastUpdatedTime(long t)
	{
		lastUpdated = t;
	}
	
	public void setUnconnectable()
	{
		unconnectable = true;
	}
	
	public void setUnresponsive()
	{
		unresponsive = true;
	}
	
	public void addAddress(Address a)
	{
		boolean alreadyHasAddress = false;
		for(int i = 0; i < addresses.size(); i++)
			if(addresses.get(i).equals(a))
				alreadyHasAddress = true;
		if(!alreadyHasAddress)
			addresses.add(a);
	}
	
	public void addRepeater(int id)
	{
		if(!repeaters.contains(id))
			repeaters.add(id);
	}
	
	public void addServiceInformation(ServiceInformation si)
	{
		boolean alreadyHasAddress = false;
		for(int i = 0; i < services.size(); i++)
			if(services.get(i).getName().equals(si.getName()))
				alreadyHasAddress = true;
		if(!alreadyHasAddress)
			services.add(si);
	}
	
	public int getNodeId()
	{
		return nodeId;
	}
	
	public int getAddressCount()
	{
		return addresses.size();
	}
	
	public Address getAddress(int i)
	{
		return addresses.get(i);
	}
		
	public boolean containsAddress(Address a)
	{
		for(int i = 0; i < addresses.size(); i++)
			if(addresses.get(i).equals(a))
				return true;
		return false;
	}
	
	public Connection getConnection()
	{
		return connection;
	}
	
	public long getLastDiscoverySentTime()
	{
		return lastSentDiscovery;
	}
	
	public long getLastAdvertisedTime()
	{
		return lastUpdated;
	}
	
	public int getRandomRepeater()
	{
		Random r = new Random();
		if(repeaters.size() > 0)
			return repeaters.get(r.nextInt(repeaters.size()));
		return 0;
	}
	
	public ServiceInformation getServiceInformation(String sn)
	{
		for(int i = 0; i < services.size(); i++)
			if(services.get(i).getName().equals(sn))
				return services.get(i);
		return null;
	}
	
	public boolean isUnconnectable()
	{
		return unconnectable;
	}
	
	public boolean isUnresponsive()
	{
		return unresponsive;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Id        : " + nodeId + "\r\n");
		for(int i = 0; i < addresses.size(); i++)
			sb.append("Address   : " + addresses.get(i) + "\r\n");
		for(int i = 0; i < repeaters.size(); i++)
			sb.append("Repeater  : " + repeaters.get(i) + "\r\n");
		for(int i = 0; i < services.size(); i++)
			sb.append("Service   : " + services.get(i) + "\r\n");
		sb.append("Connection: " + connection + "\r\n");
		return sb.toString();
	}

}
