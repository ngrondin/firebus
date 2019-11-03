package com.nic.firebus.adapters.http;


import javax.servlet.http.HttpServlet;

import com.nic.firebus.utils.DataMap;

public abstract class Handler extends HttpServlet 
{
	private static final long serialVersionUID = 1L;

	protected DataMap handlerConfig;
	
	
	public Handler(DataMap c)
	{
		handlerConfig = c;
	}

}
