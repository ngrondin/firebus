package com.nic.firebus.adapters;

import com.nic.firebus.Firebus;
import com.nic.firebus.utils.JSONObject;

public class Adapter 
{
	protected Firebus node;
	protected JSONObject config;
	
	public Adapter(Firebus n, JSONObject c)
	{
		node = n;
		config = c;
	}
}
