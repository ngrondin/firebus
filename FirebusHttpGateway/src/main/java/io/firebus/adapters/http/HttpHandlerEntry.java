package io.firebus.adapters.http;

import io.firebus.adapters.http.handlers.InboundHandler;

public class HttpHandlerEntry {
	
	public String path;
	public boolean exactPath;
	public String method;
	public String contentType;
	public InboundHandler handler;
	
	public HttpHandlerEntry(String p, String m, String ct, InboundHandler h)
	{
		path = p;
		method = m;
		contentType = ct;
		handler = h;
		if(path != null) {
			if(path.endsWith("/*")) {
				path = path.substring(0, path.length() - 2);
				exactPath = false;
			} else if(path.endsWith("/")) {
				path = path.substring(0, path.length() - 1);
				exactPath = true;
			} else {
				exactPath = true;
			}
		} else {
			exactPath = false;
		}
	}

}
