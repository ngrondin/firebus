package com.nic.firebus.distributables;

import com.nic.firebus.Node;
import com.nic.firebus.utils.JSONObject;

public class DistributableFunction 
{
	protected Node node;
	protected JSONObject config;
	
	public DistributableFunction(Node n, JSONObject c)
	{
		node = n;
		config = c;
	}
}
