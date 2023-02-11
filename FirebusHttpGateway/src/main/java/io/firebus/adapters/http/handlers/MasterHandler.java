package io.firebus.adapters.http.handlers;


import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import io.firebus.adapters.http.HttpHandlerEntry;
import io.firebus.adapters.http.HttpRequest;
import io.firebus.adapters.http.HttpResponse;
import io.firebus.adapters.http.Tools;

@SuppressWarnings("restriction")
public class MasterHandler implements com.sun.net.httpserver.HttpHandler 
{
	protected List<HttpHandlerEntry> handlerEntries; 
	protected InboundHandler defaultHandler;
	protected String rootForward;

	public MasterHandler()
	{
		handlerEntries = new ArrayList<HttpHandlerEntry>();
		rootForward = null;
	}
	
	public void addHttpHandler(String path, String method, String contentType, InboundHandler handler)
	{
		handlerEntries.add(new HttpHandlerEntry(path, method, contentType, handler));
	}
	
	public void setDefaultHander(InboundHandler dh)
	{
		defaultHandler = dh;
	}
	
	public void setRootForward(String path)
	{
		rootForward = path;
	}
	
	public void handle(HttpExchange exchange) throws IOException {
		String contextPath = exchange.getHttpContext().getPath();
		if(contextPath.equals("/")) contextPath = "";
		URI uri = exchange.getRequestURI();
		String path = uri.getPath().substring(contextPath.length());
		if(path.endsWith("/")) path = path.substring(0, path.length() - 1);
		String method = exchange.getRequestMethod();
		Headers headers = exchange.getRequestHeaders();
		String contentType = headers.getFirst("content-type");
				
		if(path.equals("/") && rootForward != null)
			path = rootForward;

		HttpHandlerEntry bestEntry = null;
		int bestPoints = 0;

		for(HttpHandlerEntry entry : handlerEntries)  {
			int matchPoints = 0;
			boolean disqualified = false;
			if(entry.path != null) {
				if((entry.exactPath && path.equals(entry.path)) || (!entry.exactPath && (path + "/").startsWith(entry.path + "/"))) 
					matchPoints++;
				else
					disqualified = true;
			} 
			
			if(entry.method != null) {
				if(entry.method.equalsIgnoreCase(method))
					matchPoints++;
				else
					disqualified = true;
			} 
			
			if(entry.contentType != null) {
				if(contentType != null && (contentType.equalsIgnoreCase(entry.contentType) || contentType.startsWith(entry.contentType + ";")))
					matchPoints++;
				else
					disqualified = true;
			} 
			
			if(!disqualified && matchPoints > bestPoints) {
				bestPoints = matchPoints;
				bestEntry = entry;
			}
		}			
		
		InboundHandler handler = defaultHandler;
		String handlerContextPath = contextPath;
		if(bestEntry != null) {
			handler = bestEntry.handler;
			handlerContextPath += bestEntry.path;
		}
		if(handler != null) {
			HttpRequest httpRequest = new HttpRequest(method, uri, headers, exchange.getRequestBody(), handlerContextPath);
			HttpResponse httpResponse = handler.service(httpRequest);
			
			long bodySize = httpResponse.getBodySize();
			for(String name : httpResponse.getHeaderNames())
				exchange.getResponseHeaders().add(name, httpResponse.getHeader(name));
			exchange.sendResponseHeaders(httpResponse.getStatus(), bodySize);
			if(bodySize >= 0)
				Tools.pipeStreams(httpResponse.getBodyInputStream(), exchange.getResponseBody());
			exchange.close();
		}
		else
		{
			String msg = "No mapping.";
			exchange.sendResponseHeaders(404, msg.length());
			exchange.getResponseBody().write(msg.getBytes());
			exchange.close();
		}	
	}	
}