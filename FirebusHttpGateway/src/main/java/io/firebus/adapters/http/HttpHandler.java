package io.firebus.adapters.http;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataMap;

public abstract class HttpHandler extends Handler 
{
	protected SecurityHandler securityHandler;
	protected String path;

	public HttpHandler(HttpGateway gw, Firebus f, DataMap c) 
	{
		super(gw, f, c);
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
	
	public SecurityHandler getSecurityHandler() {
		return securityHandler;
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

	public static void enrichFirebusRequestDefault(HttpServletRequest req, Payload payload) {
		Map<String, String> params = getParams(req);
		for(String name : params.keySet()) {
			if(name.toLowerCase().startsWith("firebus-")) {
				String shortName = name.toLowerCase().substring(8);
				payload.metadata.put(shortName, req.getHeader(name));
			}			
		}
	}
	
	public static Map<String, String> getParams(HttpServletRequest req) {
		Map<String, String> params = new HashMap<String, String>();
		Enumeration<String> e1 = req.getHeaderNames();
		while(e1.hasMoreElements()) {
			String name = e1.nextElement();
			params.put(name, req.getHeader(name));
		}
		Enumeration<String> e2 = req.getParameterNames();
		while(e2.hasMoreElements()) {
			String name = e2.nextElement();
			params.put(name, req.getParameter(name));
		}
		return params;
	}

	
	protected abstract void httpService(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
	

}
