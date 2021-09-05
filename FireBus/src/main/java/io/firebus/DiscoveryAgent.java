package io.firebus;

import io.firebus.data.DataMap;

public abstract class DiscoveryAgent extends Thread 
{
	protected NodeCore nodeCore;
	protected DataMap config;

	public DiscoveryAgent()
	{
	}

	public DiscoveryAgent(NodeCore nc)
	{
		setNodeCore(nc);
	}
	
	public DiscoveryAgent(NodeCore nc, DataMap c)
	{
		setConfig(c);
		setNodeCore(nc);
	}	
	
	public void setNodeCore(NodeCore nc)
	{
		nodeCore = nc;
		if(nodeCore != null) {
			init();
			start();
		}
	}
	
	public void setConfig(DataMap c)
	{
		config = c;
	}
	
	public void setFirebus(Firebus fb)
	{
		fb.addDiscoveryAgent(this);
	}
	
	public abstract void init();
	
	public abstract void run();
	
	public abstract void close();
}
