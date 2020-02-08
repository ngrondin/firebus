package io.firebus.adapters.http;

public class HttpHandlerEntry {
	
	public String path;
	public String method;
	public HttpHandler handler;
	
	public HttpHandlerEntry(String p, String m, HttpHandler h)
	{
		path = p;
		method = m;
		handler = h;
	}

}
