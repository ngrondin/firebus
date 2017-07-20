package com.nic.firebus;

import java.util.HashMap;
import java.util.Iterator;

public class Directory 
{
	protected HashMap<Integer, NodeInformation> nodes;
	//protected ArrayList<Address> unresolvedAddresses;

	public Directory()
	{
		nodes = new HashMap<Integer, NodeInformation>();
		//unresolvedAddresses = new ArrayList<Address>();
	}
	
	public NodeInformation getNode(int id)
	{
		return nodes.get(id);
	}
	
	public NodeInformation getOrCreateNode(int id)
	{
		NodeInformation ni = getNode(id);
		if(ni == null)
			ni = new NodeInformation(id);
		addNode(ni);
		return ni;
	}
	
	public NodeInformation getNodeByConnection(Connection c)
	{
		Iterator<Integer> it = nodes.keySet().iterator();
		while(it.hasNext())
		{
			int id = it.next();
			if(nodes.get(id).getConnection() == c)
				return nodes.get(id);
		}
		return null;
	}

	/*
	public ArrayList<Address> getUnresolvedAddresses()
	{
		return unresolvedAddresses;
	}
*/
	public void addNode(NodeInformation n)
	{
		nodes.put(n.getNodeId(), n);
	}
	/*
	public void addUnresolvedAddress(Address a)
	{
		unresolvedAddresses.add(a);
	}
	
	public void dropUnresolvedAddress(Address a)
	{
		unresolvedAddresses.remove(a);
	}
	*/
}
