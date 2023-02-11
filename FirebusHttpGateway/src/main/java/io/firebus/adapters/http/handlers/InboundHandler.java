package io.firebus.adapters.http.handlers;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import io.firebus.Firebus;
import io.firebus.adapters.http.HttpRequest;
import io.firebus.adapters.http.HttpResponse;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.exceptions.FunctionUnavailableException;
import io.firebus.utils.StackUtils;

public abstract class InboundHandler extends Handler 
{
	private Logger logger = Logger.getLogger("io.firebus.adapters.http");
	
	protected SecurityHandler securityHandler;
	protected String path;

	public InboundHandler(Firebus f, DataMap c) 
	{
		super(f, c);
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
	
	protected HttpResponse service(HttpRequest req) 
	{
		HttpResponse resp = null;
		if(req.getMethod().equals("OPTIONS"))
		{
			String origin = req.getHeaderFirstValue("Origin");
			resp = new HttpResponse(HttpServletResponse.SC_NO_CONTENT);
			if(origin != null) resp.setHeader("Access-Control-Allow-Origin", req.getHeaderFirstValue("Origin"));
			resp.setHeader("Access-Control-Allow-Credentials", "true");
			resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
			resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
		}
		else
		{
			boolean allowed = securityHandler != null ? securityHandler.checkAndEnrichHttpRequest(req) : true;
			if(allowed) {
				try {
					resp = httpService(req);
				} catch (Exception e) {
					if(!(e instanceof FunctionErrorException || e instanceof FunctionTimeoutException || e instanceof FunctionUnavailableException))
						logger.severe("Error processing inbound : " + e.getMessage() + "\r\n" + StackUtils.toString(e.getStackTrace()));
					int errorCode = (e instanceof FunctionErrorException ? ((FunctionErrorException)e).getErrorCode() : 0);
					resp = new HttpResponse(errorCode);
					if(req.accepts("application/json")) {
						DataMap body = new DataMap("error", e.getMessage());
						resp.setBody(body.toString());
					} else if(req.accepts("text/html")) {
						resp.setBody("<div>" + e.getMessage() + "</div>");
					}
				}
			} else {
				resp = securityHandler.produceUnauthenticatedHttpResponse(req);
			}
		}
		resp.setHeader("Cache-Control", "no-cache");
		return resp;
	}
	
	protected abstract HttpResponse httpService(HttpRequest req) throws Exception;

}
