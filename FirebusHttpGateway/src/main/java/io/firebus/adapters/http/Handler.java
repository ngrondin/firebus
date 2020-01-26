package com.nic.firebus.adapters.http;


import javax.servlet.http.HttpServlet;

import com.nic.firebus.Firebus;
import com.nic.firebus.utils.DataMap;

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
