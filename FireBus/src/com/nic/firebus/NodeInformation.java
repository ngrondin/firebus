package com.nic.firebus;

import java.util.ArrayList;
import java.util.Random;

public class NodeInformation 
{
	protected int nodeId;
	protected Connection connection;
	protected ArrayList<Address> addresses;
	protected ArrayList<Integer> repeaters;
	protected ArrayList<ServiceInformation> services;
	protected long lastSentDiscovery;
	protected long lastAdvertised;
	
	
	public NodeInformation(int ni)
	{
		nodeId = ni;
		initialise();
	}
	/*
	public NodeInformation(Address a)
	{
		initialise();
		addAddress(a);
	}
	*/
	protected void initialise()
	{
		services = new ArrayList<ServiceInformation>();
		repeaters = new ArrayList<Integer>();
		addresses = new ArrayList<Address>();
	}
	/*
	public void setNodeId(int i)
	{
		nodeId = i;
	}
	*/
	public void setConnection(Connection c)
	{
		connection = c;
	}
	
	public void setLastDiscoverySentTime(long t)
	{
		lastSentDiscovery = t;
	}
	
	public void setLastAdvertisedTime(long t)
	{
		lastAdvertised = t;
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
			if(services.get(i).getServiceName().equals(si.getServiceName()))
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
		return lastAdvertised;
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
			if(services.get(i).getServiceName().equals(sn))
				return services.get(i);
		return null;
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
