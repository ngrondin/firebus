package io.firebus.information;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import io.firebus.Address;

public class NodeInformation 
{
	protected int nodeId;
	protected ArrayList<Address> addresses;
	protected ArrayList<Integer> repeaters;
	protected HashMap<String, FunctionInformation> functions;
	protected long lastSentDiscovery;
	protected long lastUpdated;
	protected int rating;
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
		functions = new HashMap<String, FunctionInformation>();
		unconnectable = false;
		unresponsive = false;
		rating = 0;
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
	
	public void addFunctionInformation(String fn, FunctionInformation si)
	{
		if(!functions.containsKey(fn))
			functions.put(fn, si);
	}
	
	public int getFunctionCount()
	{
		return functions.size();
	}

	public void removeFunctionInformation(String fn)
	{
		if(functions.containsKey(fn))
			functions.remove(fn);
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
	
	public FunctionInformation getFunctionInformation(String sn)
	{
		return functions.get(sn);
	}
	
	public boolean isUnconnectable()
	{
		return unconnectable;
	}
	
	public boolean isUnresponsive()
	{
		return unresponsive;
	}
	
	public void reduceRating(int r) {
		rating -= r;
	}
	
	public void resetRating() {
		rating = 0;
	}
	
	public int getRating() {
		return rating;
	}


	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Id        : " + nodeId + "   [" + getRating() + "]\r\n");
		sb.append("Rating    : " + getRating() + "\r\n");
		for(int i = 0; i < addresses.size(); i++)
			sb.append("Address   : " + addresses.get(i) + "\r\n");
		for(int i = 0; i < repeaters.size(); i++)
			sb.append("Repeater  : " + repeaters.get(i) + "\r\n");
		Iterator<String> it = functions.keySet().iterator();
		while(it.hasNext()) {
			String fn = it.next();
			sb.append("Function   : " + fn + "   [" + functions.get(fn).getRating() + "]\r\n");
		}
		return sb.toString();
	}

}
