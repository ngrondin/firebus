package com.nic.firebus;

import java.util.HashMap;
import java.util.Iterator;

public class FunctionManager 
{
	protected Node node;
	protected HashMap<String, BusFunction> functions;
	
	public FunctionManager(Node n)
	{
		node = n;
		functions = new HashMap<String, BusFunction>();
	}
	
	public void addFunction(String n, BusFunction f)
	{
		functions.put(n, f);
	}
	
	public BusFunction find(String n)
	{
		return functions.get(n);
	}
	
	public String getFunctionAdvertisementString(String n)
	{
		StringBuilder sb = new StringBuilder();
		Iterator<String> it = functions.keySet().iterator();
		while(it.hasNext())
		{
			String functionName = it.next();
			if(functionName.equals(n)  ||  n == null)
			{
				BusFunction f = functions.get(functionName);
				if(f != null)
				{
					sb.append(node.getNodeId());
					if(f instanceof ServiceProvider)
						sb.append(",s,");
					if(f instanceof Publisher)
						sb.append(",p,");
					if(f instanceof Consumer)
						sb.append(",c,");
					sb.append(functionName);
					sb.append("\r\n");
				}
			}
		}
		return sb.toString();
	}
	
	public void requestService(String functionName, byte[] payload, int correlation)
	{
		BusFunction f = functions.get(functionName);
		if(f instanceof ServiceProvider)
			new FunctionWorker(f, payload, node, correlation);
	}
	
	public void consume(String name, byte[] payload)
	{
		BusFunction f = functions.get(name);
		if(f instanceof Consumer)
			new FunctionWorker(f, payload);
	}

}
