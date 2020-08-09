package io.firebus.adapters.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.firebus.Firebus;
import io.firebus.utils.DataMap;

public abstract class HttpHandler extends Handler 
{
	protected SecurityHandler securityHandler;
	protected String path;

	public HttpHandler(DataMap c, Firebus fb) 
	{
		super(c, fb);
		path = handlerConfig.getString("path");
	}
	
	public void setSecurityHandler(SecurityHandler sh)
	{
		securityHandler = sh;
	}

	protected String getHttpHandlerPath() 
	{
		String path = handlerConfig.getString("path");
		if(path.endsWith("/*"))
			path = path.substring(0, path.length() - 2);
		return path;
	}
	
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Cache-Control", "no-cache");

		if(req.getMethod().equals("OPTIONS"))
		{
			resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
			resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
			resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
		}
		else
		{
			boolean allowed = true;
			if(securityHandler != null)
				allowed = securityHandler.checkHttpRequest(req, resp);
			if(allowed)
				httpService(req, resp);
		}		
	}
	
	protected abstract void httpService(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
	

}
