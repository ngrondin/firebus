package io.firebus.adapters.http;


import io.firebus.Firebus;
import io.firebus.utils.DataMap;

public abstract class AuthValidationHandler extends HttpHandler
{
	protected String publicHost;
	protected SecurityHandler _securityHandler;
	
	public AuthValidationHandler(HttpGateway gw, Firebus f, DataMap c) 
	{
		super(gw, f, c);
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
