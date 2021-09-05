package io.firebus.adapters;

import io.firebus.Firebus;
import io.firebus.data.DataMap;

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
