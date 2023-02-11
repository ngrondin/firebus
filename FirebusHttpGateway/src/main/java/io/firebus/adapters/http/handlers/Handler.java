package io.firebus.adapters.http.handlers;


import io.firebus.Firebus;
import io.firebus.data.DataMap;

public abstract class Handler
{
	protected DataMap handlerConfig;
	protected Firebus firebus;
	
	public Handler(Firebus f, DataMap c)
	{
		handlerConfig = c;
		firebus = f;
	}
	
}
