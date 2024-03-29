package io.firebus.information;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import io.firebus.Address;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;

public class NodeInformation 
{
	protected int nodeId;
	protected ArrayList<Address> addresses;
	protected ArrayList<Integer> repeaters;
	protected HashMap<String, FunctionInformation> functions;
	protected long lastSentDiscovery;
	protected long lastUpdated;
	protected int rating;

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
		rating = 100;
	}

	public void setLastDiscoverySentTime(long t)
	{
		lastSentDiscovery = t;
	}
	
	public void setLastUpdatedTime(long t)
	{
		lastUpdated = t;
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
		Logger.info("fb.nodeinfo.removing", new DataMap("function", fn));
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
	
	public int getRepeaterCount() 
	{
		return repeaters.size();
	}
	
	public int getRepeater(int i) 
	{
		if(i < repeaters.size() && i >= 0) 
			return repeaters.get(i);
		else 
			return 0;
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
	
	public synchronized void didNotRespond() 
	{
		rating--;
		if(rating < 0) rating = 0;
	}
	
	public synchronized void responded() 
	{
		rating++;
		if(rating > 100) rating = 100;
	}
	
	public boolean shouldRemove()
	{
		return rating == 0;
	}
	
	public int getRating() 
	{
		return rating;
	}


	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Id         : " + nodeId + "   [" + getRating() + "]\r\n");
		for(int i = 0; i < addresses.size(); i++)
			sb.append("Address    : " + addresses.get(i) + "\r\n");
		for(int i = 0; i < repeaters.size(); i++)
			sb.append("Repeater   : " + repeaters.get(i) + "\r\n");
		Iterator<String> it = functions.keySet().iterator();
		while(it.hasNext()) {
			String fn = it.next();
			FunctionInformation fi = functions.get(fn);
			sb.append("Function   : " + fi + "\r\n");
		}
		return sb.toString();
	}
	
	public DataMap getStatus()
	{
		DataMap status = new DataMap();
		DataList addList = new DataList();
		for(Address a: addresses) 
			addList.add(a.toString());
		status.put("addresses", addList);
		DataList rptList = new DataList();
		for(Integer i: repeaters) 
			rptList.add(i);
		status.put("repeaters", rptList);		
		DataMap funcMap = new DataMap();
		for(String fn: functions.keySet()) 
			funcMap.put(fn, functions.get(fn).getStatus());
		status.put("functions", funcMap);
		status.put("rating", rating);
		return status;
	}

}
