package io.firebus.adapters.http.handlers;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.impl.client.CloseableHttpClient;

import io.firebus.Payload;
import io.firebus.adapters.http.HttpRequest;
import io.firebus.adapters.http.HttpResponse;
import io.firebus.data.DataMap;

public abstract class SecurityHandler {
	protected DataMap config;
	protected CloseableHttpClient httpClient;
	protected List<AuthValidationHandler> authValidationHandlers;
	
	public SecurityHandler(DataMap c, CloseableHttpClient hc) 
	{
		config = c;
		httpClient = hc;
		authValidationHandlers = new ArrayList<AuthValidationHandler>();
	}
	
	public void addAuthValidationHandler(AuthValidationHandler avh)
	{
		authValidationHandlers.add(avh);
	}
	
	protected HttpResponse produceUnauthenticatedHttpResponse(HttpRequest req) {
		String path = req.getFullPath();
		HttpResponse resp = null;
		if(req.accepts("text/html")) {
			if(authValidationHandlers.size() > 1) {
				resp = new HttpResponse(200);
		        StringBuilder sb = new StringBuilder();
		        sb.append("<html><head><title>Login</title><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><style>");
		        sb.append("body{}");
		        sb.append(".main{position:fixed; top:50%; left: 50%; transform: translate(-50%, -50%); font-family:sans-serif; font-size:larger; border:1px solid lightgrey; padding:15px; border-radius:5px;}");
		        sb.append(".title{color:grey; padding:5px;}");
		        sb.append(".option{display:flex; flex-direction:row; align-items:center;padding:5px;}");
		        sb.append("a{display:flex; flex-direction:row; align-items:center;}");
		        sb.append("a:link {color:black; text-decoration:none;} a:visited  {color:black; text-decoration:none;} a:hover {color:black; text-decoration:none;} a:active {color:black; text-decoration:none;}");
		        sb.append("img {padding-right:10px;}");
		        sb.append("</style></head>");
		        sb.append("<body><div class=\"main\"><div class=\"title\">Login with</div>");
		        for(AuthValidationHandler avh: authValidationHandlers) {
		        	sb.append("<div class=\"option\"><a href=\"" + avh.getLoginURL(path) + "\"><img src=\"" + avh.getIcon() + "\"><div>" + avh.getLabel() + "</div></a></div>");
		        }
		        sb.append("</div></body></html>");
		        resp.setBody(sb.toString());
			} else if(authValidationHandlers.size() == 1) {
				resp = new HttpResponse(301);
				resp.setHeader("Location", authValidationHandlers.get(0).getLoginURL(path));
			} else {
				resp = new HttpResponse(401);
			}			
		} else {
			resp = new HttpResponse(401);
		}
		return resp;
	}
	
	public abstract boolean checkAndEnrichHttpRequest(HttpRequest req);
	
	public abstract void enrichFirebusRequest(HttpRequest req, Payload payload);
	
	public abstract void enrichAuthenticatedHttpResponse(String username, HttpResponse resp);
	
	public abstract void enrichLogoutResponse(HttpResponse resp);
}
