package io.firebus.adapters.http.handlers;


import org.apache.http.impl.client.CloseableHttpClient;

import io.firebus.Firebus;
import io.firebus.data.DataMap;

public abstract class AuthValidationHandler extends InboundHandler
{
	protected String publicHost;
	protected SecurityHandler _securityHandler;
	protected CloseableHttpClient httpClient;
	
	public AuthValidationHandler(Firebus f, DataMap c, CloseableHttpClient hc) 
	{
		super(f, c);
		httpClient = hc;
	}
	
	public String getLabel()
	{
		return handlerConfig.getString("label");
	}

	public String getIcon()
	{
		return handlerConfig.getString("icon");
	}

	public void setPublicHost(String ph)
	{
		publicHost = ph;
	}
	
	public void setSecurityHandler(SecurityHandler sh)
	{
		_securityHandler = sh;
	}
	
	public abstract String getLoginURL(String originalPath);

}
