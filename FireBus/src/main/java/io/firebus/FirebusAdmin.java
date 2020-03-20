package io.firebus;

import io.firebus.information.NodeInformation;

public class FirebusAdmin extends Firebus
{
	public FirebusAdmin()
	{
		super();
	}
	
	public FirebusAdmin(int p)
	{
		super(p);
	}
	
	public FirebusAdmin(String network, String password)
	{
		super(network, password);
	}

	public FirebusAdmin(int p, String network, String password)
	{
		super(p, network, password);
	}

	public NodeInformation[] getNodeList()
	{
		int c = nodeCore.getDirectory().getNodeCount();
		NodeInformation[] ni = new NodeInformation[c];
		for(int i = 0; i < c; i++)
		{
			ni[i] = nodeCore.getDirectory().getNode(i);
		}
		return ni;
	}
		

}
