package com.nic.firebus.information;

import java.util.ArrayList;
import java.util.Random;

import com.nic.firebus.Address;

public class NodeInformation 
{
	protected int nodeId;
	protected ArrayList<Address> addresses;
	protected ArrayList<Integer> repeaters;
	protected ArrayList<ServiceInformation> services;
	protected ArrayList<ConsumerInformation> consumers;
	protected long lastSentDiscovery;
	protected long lastUpdated;
	protected boolean unconnectable;
	protected boolean unresponsive;

	public NodeInformation(int ni)
	{
		nodeId = ni;
		initialise();
	}

	protected void initialise()
	{
		addresses = new ArrayList<Address>();
		repeaters = new ArrayList<Integer>();
		services = new ArrayList<ServiceInformation>();
		consumers = new ArrayList<ConsumerInformation>();
		unconnectable = false;
		unresponsive = false;
	}

	/*
	public void setConnection(Connection c)
	{
		connection = c;
		if(c != null)
			unconnectable = false;
	}
	*/
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
		if(!containsAddress(a))
			addresses.add(a);
	}
	
	public void addRepeater(int id)
	{
		if(!repeaters.contains(id))
			repeaters.add(id);
	}
	
	public void addServiceInformation(ServiceInformation si)
	{
		if(getServiceInformation(si.getName()) == null)
			services.add(si);
	}

	public void addConsumerInformation(ConsumerInformation ci)
	{
		if(getConsumerInformation(ci.getName()) == null)
			consumers.add(ci);
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
	
	/*
	public Connection getConnection()
	{
		return connection;
	}
	*/
	
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
	
	public ConsumerInformation getConsumerInformation(String cn)
	{
		for(int i = 0; i < consumers.size(); i++)
			if(consumers.get(i).getName().equals(cn))
				return consumers.get(i);
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
		for(int i = 0; i < consumers.size(); i++)
			sb.append("Consumers : " + consumers.get(i) + "\r\n");
		//sb.append("Connection: " + connection + "\r\n");
		return sb.toString();
	}

}
