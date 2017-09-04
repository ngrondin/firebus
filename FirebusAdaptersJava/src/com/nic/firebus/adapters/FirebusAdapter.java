package com.nic.firebus.adapters;

import com.nic.firebus.Node;
import com.nic.firebus.utils.JSONObject;

public class FirebusAdapter
{
	protected Node node;
	protected JSONObject config;
	
	public FirebusAdapter(Node n, JSONObject c)
	{
			node = n;
			config = c;
	}
}
