package io.firebus.adapters.http;


import io.firebus.Firebus;
import io.firebus.utils.DataMap;

public abstract class Handler
{
	protected DataMap handlerConfig;
	protected Firebus firebus;
	
	public Handler(DataMap c, Firebus fb)
	{
		handlerConfig = c;
		firebus = fb;
	}
	
}
