package io.firebus.adapters.http;


import javax.servlet.http.HttpServletRequest;

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
	
	public String getHostUrl(HttpServletRequest req) {
		String scheme = req.getScheme();
		String xfp = req.getHeader("X-Forwarded-Proto");
		String cffp = req.getHeader("cloudfront-forwarded-proto");
		int port = req.getServerPort();
		boolean ishttps = (scheme != null && scheme.equals("https")) || (xfp != null && xfp.equals("https")) || (cffp != null && cffp.equals("https")) || port == 443;
		String url = (ishttps ? "https" : "http")  + "://" + req.getServerName();
		if(port != 80 && port != 443)
			url = url + ":" + req.getServerPort();
		return url;
	}
	
	public abstract String getLoginURL(HttpServletRequest req, String originalPath);
	
	public abstract String getCodeURL(HttpServletRequest req);
	
	public abstract String getRefreshUrl(HttpServletRequest req, String originalPath);
	

}
