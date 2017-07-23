package com.nic.firebus;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Directory 
{
	protected ArrayList<NodeInformation> nodes;
	//protected ArrayList<Address> addresses;

	public Directory()
	{
		nodes = new ArrayList<NodeInformation>();
		//addresses = new ArrayList<Address>();
	}
	
	public NodeInformation getNodeById(int id)
	{
		if(id != 0)
			for(int i = 0; i < nodes.size(); i++)
				if(nodes.get(i).getNodeId() == id)
					return nodes.get(i);
		return null;
	}
	
	public NodeInformation getNodeByAddress(Address a)
	{
		for(int i = 0; i < nodes.size(); i++)
			if(nodes.get(i).containsAddress(a))
				return nodes.get(i);
		return null;
	}

	public NodeInformation getNodeByConnection(Connection c)
	{
		for(int i = 0; i < nodes.size(); i++)
			if(nodes.get(i).getConnection() == c)
				return nodes.get(i);
		return null;
	}
	
	
	public NodeInformation getOrCreateNodeById(int id)
	{
		NodeInformation ni = getNodeById(id);
		if(ni == null)
			ni = new NodeInformation(id);
		addNode(ni);
		return ni;
	}
	
	public NodeInformation getOrCreateNodeByAddress(Address a)
	{
		NodeInformation ni = getNodeByAddress(a);
		if(ni == null)
			ni = new NodeInformation(a);
		addNode(ni);
		return ni;
	}
	
	
	/*
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
	*/

	public void addNode(NodeInformation n)
	{
		if(!nodes.contains(n))
			nodes.add(n);
		//if(n.getAddress() != null && !addresses.contains(n.getAddress()))
		//	addAddress(n.getAddress());
	}
	
	/*
	public void addAddress(Address a)
	{
		if(!addresses.contains(a))
			addresses.add(a);
		if(a.getNodeInformation() != null && !nodes.contains(a.getNodeInformation()))
			addNode(a.getNodeInformation());
	}
	*/

	public void processAdvertisementMessage(byte[] payload)
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(payload)));
		String line;
		try 
		{
			while((line = br.readLine()) != null)
			{
				String[] parts = line.split(",");
				int id = Integer.parseInt(parts[0]);
				NodeInformation ni = getNodeById(id);
				if(ni != null)
				{
					if(parts[1].equals("a"))
					{
						ni.addAddress(new Address(parts[2], Integer.parseInt(parts[3])));
					}
					else if(parts[1].equals("s"))
					{
						String serviceName = parts[2];
						ServiceInformation si = ni.getServiceInformation(serviceName);
						if(si == null)
							si = new ServiceInformation(serviceName);
						ni.addServiceInformation(si);
					}
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
	}

	public NodeInformation findServiceProvider(String name)
	{
		for(int i = 0; i < nodes.size(); i++)
		{
			NodeInformation ni = nodes.get(i);
			if(ni.getServiceInformation(name) != null)
				return ni;
		}
		return null;
	}
	
	public ArrayList<NodeInformation> getUnresolvedAndUnconnected()
	{
		ArrayList<NodeInformation> ua = new ArrayList<NodeInformation>();
		for(int i = 0; i < nodes.size(); i++)
			if(nodes.get(i).getNodeId() == 0  &&  nodes.get(i).getConnection() == null)
				ua.add(nodes.get(i));
		return ua;		
	}
	
	public int getUnresolvedAndUnconnectedCount()
	{
		int c = 0;
		for(int i = 0; i < nodes.size(); i++)
			if(nodes.get(i).getNodeId() == 0  &&  nodes.get(i).getConnection() == null)
				c++;
		return c;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if(nodes.size() == 0)
		{
			sb.append("Directory Empty");
		}
		else
		{
			for(int i = 0; i < nodes.size(); i++)
			{
				sb.append("---------------------\r\n");
				sb.append(nodes.get(i).toString() + "\r\n\r\n");
			}
		}
		return sb.toString();
	}
}
