package com.nic.firebus.adapters;

import com.nic.firebus.Firebus;
import com.nic.firebus.utils.DataMap;

public class Adapter 
{
	protected Firebus node;
	protected DataMap config;

	public Adapter(DataMap c)
	{
		config = c;
	}
	
	public Adapter(DataMap c, Firebus n)
	{
		node = n;
		config = c;
	}
}
