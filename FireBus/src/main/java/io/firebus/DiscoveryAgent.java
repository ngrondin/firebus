package io.firebus;


public abstract class DiscoveryAgent extends Thread 
{
	protected NodeCore nodeCore;

	public DiscoveryAgent()
	{
	}

	public DiscoveryAgent(NodeCore nc)
	{
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
	
	public void setFirebus(Firebus fb)
	{
		fb.addDiscoveryAgent(this);
	}
	
	public abstract void init();
	
	public abstract void run();
	
	public abstract void close();
}
