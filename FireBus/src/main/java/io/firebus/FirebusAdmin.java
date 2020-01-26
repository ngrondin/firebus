package io.firebus;

import io.firebus.information.NodeInformation;

public class FirebusAdmin extends Firebus
{
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
