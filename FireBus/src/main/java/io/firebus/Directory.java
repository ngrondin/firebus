package io.firebus;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Logger;

import io.firebus.information.ConsumerInformation;
import io.firebus.information.FunctionInformation;
import io.firebus.information.NodeInformation;
import io.firebus.information.ServiceInformation;
import io.firebus.information.StreamInformation;

public class Directory 
{
	private Logger logger = Logger.getLogger("io.firebus");
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

	public synchronized void deleteNode(NodeInformation n)
	{
		logger.fine("Deleting Node from Directory");
		nodes.remove(n);
	}
	
	public synchronized NodeInformation getOrCreateNodeInformation(int nodeId)
	{
		NodeInformation ni = getNodeById(nodeId);
		if(ni == null)
		{
			ni = new NodeInformation(nodeId);
			nodes.add(ni);
		}
		return ni;
	}
	
	public synchronized void processDiscoveredNode(int nodeId, Address address)
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
	
	public synchronized void processNodeInformation(String ad)
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
				ni.resetRating();
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
					String name = parts[3];
					FunctionInformation fi = ni.getFunctionInformation(name);
					if(fi == null) {
						if(functionType.equals("s"))
							fi = new ServiceInformation(name);
						else if(functionType.equals("t"))
							fi = new StreamInformation(name);
						else if(functionType.equals("c"))
							fi = new ConsumerInformation(name);
						ni.addFunctionInformation(name, fi);
					}
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
	}

	
	public synchronized void processFunctionInformation(int nodeId, String functionName, byte[] payload)
	{
		NodeInformation ni = getOrCreateNodeInformation(nodeId);
		ni.resetRating();
		FunctionInformation fi = ni.getFunctionInformation(functionName);
		if(fi == null)
		{
			char type = (char)payload[0];
			if(type == 's')
				fi = new ServiceInformation(functionName);
			else if(type == 't')
				fi = new StreamInformation(functionName);
			else if(type == 'c')
				fi = new ConsumerInformation(functionName);
			ni.addFunctionInformation(functionName, fi);
		}
		fi.deserialise(payload);
	}

	
	public NodeInformation findFunction(String name)
	{
		NodeInformation bestNode = null;
		int bestNodeRating = -100000;
		for(int i = 0; i < nodes.size(); i++)
		{
			NodeInformation ni = nodes.get(i);
			FunctionInformation fi = ni.getFunctionInformation(name);
			if(fi != null) {
				if(fi.getRating() > -10) {
					if(fi.getRating() > bestNodeRating) {
						bestNode = ni;
						bestNodeRating = fi.getRating();
					}
				} else {
					ni.removeFunctionInformation(name);
					ni.reduceRating(2);
					if(ni.getFunctionCount() == 0 || ni.getRating() < -5) 
						deleteNode(ni);
				}
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
