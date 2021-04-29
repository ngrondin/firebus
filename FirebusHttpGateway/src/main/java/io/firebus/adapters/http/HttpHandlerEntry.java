package io.firebus.adapters.http;

public class HttpHandlerEntry {
	
	public String path;
	public String method;
	public String contentType;
	public HttpHandler handler;
	
	public HttpHandlerEntry(String p, String m, String ct, HttpHandler h)
	{
		path = p;
		method = m;
		contentType = ct;
		handler = h;
	}

}
