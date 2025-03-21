package io.firebus;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import io.firebus.information.ConsumerInformation;
import io.firebus.information.FunctionInformation;
import io.firebus.information.NodeInformation;
import io.firebus.information.ServiceInformation;
import io.firebus.information.StreamInformation;
import io.firebus.logging.Logger;
import io.firebus.data.DataMap;

public class Directory 
{
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
		Logger.info("fb.directory.delete", new DataMap("node", n.getNodeId()));
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
			Logger.info("fb.directory.nodediscovered", new DataMap("node",nodeId, "address", address != null ? address.toString() : null));
			NodeInformation nodeByAddress = getNodeByAddress(address);
			if(nodeByAddress != null)
				deleteNode(nodeByAddress);
			ni = new NodeInformation(nodeId);
			ni.addAddress(address);
			nodes.add(ni);
		}
		else if(!ni.containsAddress(address))
		{
			Logger.info("fb.directory.addressdiscovered", new DataMap("node",nodeId, "address", address));
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
				if(parts[1].equals("a"))
				{
					Address a = new Address(parts[2], Integer.parseInt(parts[3]));
					NodeInformation nodeByAddress = getNodeByAddress(a);
					if(nodeByAddress != null && nodeByAddress != ni)
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
							fi = new ServiceInformation(ni, name);
						else if(functionType.equals("t"))
							fi = new StreamInformation(ni, name);
						else if(functionType.equals("c"))
							fi = new ConsumerInformation(ni, name);
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
		FunctionInformation fi = ni.getFunctionInformation(functionName);
		if(fi == null)
		{
			char type = (char)payload[0];
			if(type == 's')
				fi = new ServiceInformation(ni, functionName);
			else if(type == 't')
				fi = new StreamInformation(ni, functionName);
			else if(type == 'c')
				fi = new ConsumerInformation(ni, functionName);
			ni.addFunctionInformation(functionName, fi);
		}
		fi.deserialise(payload);
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
	
	public DataMap getStatus()
	{
		DataMap status = new DataMap();
		for(NodeInformation ni: nodes) 
			status.put(String.valueOf(ni.getNodeId()), ni.getStatus());
		return status;
	}
}
