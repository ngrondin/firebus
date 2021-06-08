package io.firebus.adapters.http;


import io.firebus.Firebus;
import io.firebus.utils.DataMap;

public abstract class Handler
{
	protected HttpGateway httpGateway;
	protected DataMap handlerConfig;
	protected Firebus firebus;
	
	public Handler(HttpGateway gw, Firebus f, DataMap c)
	{
		httpGateway = gw;
		handlerConfig = c;
		firebus = f;
	}
	
}
