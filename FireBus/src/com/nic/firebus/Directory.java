package com.nic.firebus;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Logger;

public class Directory 
{
	private Logger logger = Logger.getLogger(Directory.class.getName());
	protected ArrayList<NodeInformation> nodes;

	public Directory()
	{
		nodes = new ArrayList<NodeInformation>();
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
	

	public void addNode(NodeInformation n)
	{
		logger.fine("Adding Node to Directory");
		if(!nodes.contains(n))
			nodes.add(n);
	}
	
	public void deleteNode(NodeInformation n)
	{
		logger.fine("Deleting Node to Directory");
		nodes.remove(n);
	}
	
	public int getNodeCount()
	{
		return nodes.size();
	}
	
	public void processStateMessage(String ad)
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(ad.getBytes())));
		String line;
		try 
		{
			while((line = br.readLine()) != null)
			{
				String[] parts = line.split(",");
				int id = Integer.parseInt(parts[0]);
				NodeInformation ni = getNodeById(id);
				if(ni == null)
				{
					ni = new NodeInformation(id);
					addNode(ni);
				}
				ni.setLastUpdatedTime(System.currentTimeMillis());
				if(parts[1].equals("a"))
				{
					Address a = new Address(parts[2], Integer.parseInt(parts[3]));
					NodeInformation nodeByAddress = getNodeByAddress(a);
					if(nodeByAddress != ni)
						deleteNode(nodeByAddress);
					ni.addAddress(a);
				}
				else if(parts[1].equals("f"))
				{
					String functionType = parts[2];
					if(functionType.equals("s"))
					{
						String serviceName = parts[3];
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
			if(ni.getServiceInformation(name) != null  &&  !ni.isUnresponsive())
				return ni;
		}
		return null;
	}

	public ArrayList<NodeInformation> getNodeToConnectTo()
	{
		ArrayList<NodeInformation> list = new ArrayList<NodeInformation>();
		for(int i = 0; i < nodes.size(); i++)
		{
			NodeInformation ni = nodes.get(i);
			if(ni.getConnection() == null  &&  ni.isUnconnectable() == false)
				list.add(nodes.get(i));
		}
		return list;
	}
	
	public String getDirectoryStateString(int nodeId)
	{
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < nodes.size(); i++)
		{
			NodeInformation ni = nodes.get(i);
			Connection c = ni.getConnection();
			sb.append(nodeId + ",d," + ni.getNodeId() + "," + (c != null? c.getId() : "") + "\r\n");
			for(int j = 0; j < ni.getAddressCount(); j++)
			{
				Address a = ni.getAddress(j);
				sb.append(nodeId + ",d," + ni.getNodeId() + "a" + a.getIPAddress() + "," + a.getPort() + "\r\n");
			}
		}
		return sb.toString();
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
				if(i > 0)
					sb.append("\r\n");
				sb.append("---------Directory Entry--\r\n");
				sb.append(nodes.get(i).toString());
			}
		}
		return sb.toString();
	}
}
