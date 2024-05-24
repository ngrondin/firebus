package io.firebus.adapters.http;


import io.firebus.Firebus;
import io.firebus.data.DataMap;

public abstract class IDMHandler extends InboundHandler
{
	protected SecurityHandler _securityHandler;
	protected String label;
	protected String icon;
	protected String uri;
	
	public IDMHandler(HttpGateway gw, Firebus f, DataMap c) 
	{
		super(gw, f, c);
		label = handlerConfig.getString("label");
		icon = handlerConfig.getString("icon");
		uri = handlerConfig.getString("uri");
	}
	
	public String getLabel() {
		return label;
	}

	public String getIcon() {
		return icon;
	}
	
	public String getUri() {
		return uri;
	}
	
	
	public void setSecurityHandler(SecurityHandler sh)
	{
		_securityHandler = sh;
	}
	
	public abstract String getLoginURL(String originalPath);
	
	public abstract String geCodeURL();
	
	public abstract String geRefereshURL(String originalPath);
	

}
