package io.firebus.adapters.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.firebus.Firebus;
import io.firebus.utils.DataMap;

public abstract class HttpHandler extends Handler 
{

	public HttpHandler(DataMap c, Firebus fb) 
	{
		super(c, fb);
	}

	protected String getHttpHandlerPath() 
	{
		String path = handlerConfig.getString("path");
		if(path.endsWith("/*"))
			path = path.substring(0, path.length() - 2);
		return path;
	}
	
	protected abstract void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
}
