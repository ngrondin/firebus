package io.firebus;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Logger;

import io.firebus.information.ConsumerInformation;
import io.firebus.information.NodeInformation;
import io.firebus.information.ServiceInformation;

public class Directory 
{
	private Logger logger = Logger.getLogger("com.nic.firebus");
	protected ArrayList<NodeInformation> nodes;

	public Directory()
	{
		nodes = new ArrayList<NodeInformation>();
	}
	
	public int getNodeCount()
	{
		return nodes.size();
	}
	
	public NodeInformation getNode(int index)
	{
		return nodes.get(index);
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

	public void deleteNode(NodeInformation n)
	{
		logger.fine("Deleting Node from Directory");
		nodes.remove(n);
	}
	
	public NodeInformation getOrCreateNodeInformation(int nodeId)
	{
		NodeInformation ni = getNodeById(nodeId);
		if(ni == null)
		{
			ni = new NodeInformation(nodeId);
			nodes.add(ni);
		}
		return ni;
	}
	
	public void processDiscoveredNode(int nodeId, Address address)
	{
		NodeInformation ni = getNodeById(nodeId);
		if(ni == null)
		{
			logger.fine("Node discovered : " + nodeId + " at address " + address);
			NodeInformation nodeByAddress = getNodeByAddress(address);
			if(nodeByAddress != null)
				deleteNode(nodeByAddress);
			ni = new NodeInformation(nodeId);
			ni.addAddress(address);
			nodes.add(ni);
		}
		else if(!ni.containsAddress(address))
		{
			logger.fine("New address discovered for node : " + nodeId + " at address " + address);
			ni.addAddress(address);
		}		
	}
	
	public void processNodeInformation(String ad)
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
					nodes.add(ni);
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
						ni.addServiceInformation(serviceName, si);
					}
					else if(functionType.equals("c"))
					{
						String consumerName = parts[3];
						ConsumerInformation ci = ni.getConsumerInformation(consumerName);
						if(ci == null)
							ci = new ConsumerInformation(consumerName);
						ni.addConsumerInformation(consumerName, ci);
					}
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
	}

	
	public void processServiceInformation(int nodeId, String serviceName, byte[] payoad)
	{
		NodeInformation ni = getOrCreateNodeInformation(nodeId);
		ServiceInformation si = ni.getServiceInformation(serviceName);
		if(si == null)
		{
			si = new ServiceInformation(serviceName);
			ni.addServiceInformation(serviceName, si);
		}
		si.deserialise(payoad);
	}
	
	public NodeInformation findServiceProvider(String name)
	{
		NodeInformation bestNode = null;
		int bestNodeRating = 0;
		for(int i = 0; i < nodes.size(); i++)
		{
			NodeInformation ni = nodes.get(i);
			ServiceInformation si = ni.getServiceInformation(name);
			if(si != null  &&  !ni.isUnresponsive()  &&  si.getRating() > bestNodeRating)
			{
				bestNode = ni;
				bestNodeRating = si.getRating();
			}
		}
		return bestNode;
	}


	public String getDirectoryStateString(int nodeId)
	{
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < nodes.size(); i++)
		{
			NodeInformation ni = nodes.get(i);
			sb.append(nodeId + ",d," + ni.getNodeId() + ",\r\n");
			for(int j = 0; j < ni.getAddressCount(); j++)
			{
				Address a = ni.getAddress(j);
				sb.append(nodeId + ",d," + ni.getNodeId() + ",a," + a.getIPAddress() + "," + a.getPort() + "\r\n");
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
