package com.nic.firebus.information;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import com.nic.firebus.Address;

public class NodeInformation 
{
	protected int nodeId;
	protected ArrayList<Address> addresses;
	protected ArrayList<Integer> repeaters;
	protected HashMap<String, ServiceInformation> services;
	protected HashMap<String, ConsumerInformation> consumers;
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
		services = new HashMap<String, ServiceInformation>();
		consumers = new HashMap<String, ConsumerInformation>();
		unconnectable = false;
		unresponsive = false;
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
		if(a != null  &&  !containsAddress(a))
			addresses.add(a);
	}
	
	public void removeAddress(Address a)
	{
		if(containsAddress(a))
			addresses.remove(a);
	}
	
	public void addRepeater(int id)
	{
		if(!repeaters.contains(id))
			repeaters.add(id);
	}
	
	public void addServiceInformation(String sn, ServiceInformation si)
	{
		if(!services.containsKey(sn))
			services.put(sn, si);
	}

	public void addConsumerInformation(String cn, ConsumerInformation ci)
	{
		if(!consumers.containsKey(cn))
			consumers.put(cn, ci);
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
		return services.get(sn);
	}
	
	public ConsumerInformation getConsumerInformation(String cn)
	{
		return consumers.get(cn);
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
		Iterator<String> it = services.keySet().iterator();
		while(it.hasNext())
			sb.append("Service   : " + it.next() + "\r\n");
		it = consumers.keySet().iterator();
		while(it.hasNext())
			sb.append("Consumers : " + it.next() + "\r\n");
		return sb.toString();
	}

}
