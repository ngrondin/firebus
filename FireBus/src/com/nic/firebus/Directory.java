package com.nic.firebus;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;

public class Directory 
{
	protected HashMap<Integer, NodeInformation> nodes;

	public Directory()
	{
		nodes = new HashMap<Integer, NodeInformation>();
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


	public void addNode(NodeInformation n)
	{
		nodes.put(n.getNodeId(), n);
	}

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
				NodeInformation ni = getOrCreateNode(id);
				if(parts[1].equals("a"))
				{
					ni.setInetAddress(new Address(parts[2], Integer.parseInt(parts[3])));
				}
				else if(parts[1].equals("s"))
				{
					String serviceName = parts[2];
					ServiceInformation si = ni.getService(serviceName);
					if(si == null)
						si = new ServiceInformation(serviceName);
					ni.addService(si);
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
		Iterator<Integer> it = nodes.keySet().iterator();
		while(it.hasNext())
		{
			NodeInformation ni = nodes.get(it.next());
			if(ni.getService(name) != null)
				return ni;
		}
		return null;
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
			Iterator<Integer> it = nodes.keySet().iterator();
			while(it.hasNext())
			{
				sb.append("---------------------\r\n");
				sb.append(nodes.get(it.next()).toString() + "\r\n\r\n");
			}
		}
		return sb.toString();
	}
}
