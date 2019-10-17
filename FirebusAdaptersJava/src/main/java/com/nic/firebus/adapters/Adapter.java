package com.nic.firebus.adapters;

import com.nic.firebus.Firebus;
import com.nic.firebus.utils.DataMap;

public class Adapter 
{
	protected Firebus node;
	protected DataMap config;
	
	public Adapter(Firebus n, DataMap c)
	{
		node = n;
		config = c;
	}
}
