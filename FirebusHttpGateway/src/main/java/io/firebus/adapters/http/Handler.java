package io.firebus.adapters.http;


import javax.servlet.http.HttpServlet;

import io.firebus.Firebus;
import io.firebus.utils.DataMap;

public abstract class Handler extends HttpServlet 
{
	private static final long serialVersionUID = 1L;

	protected DataMap handlerConfig;
	protected Firebus firebus;
	
	public Handler(DataMap c, Firebus fb)
	{
		handlerConfig = c;
		firebus = fb;
	}

}
