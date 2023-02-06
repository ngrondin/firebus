package io.firebus.adapters.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HttpResponse {
	protected int code;
	protected Map<String, String> headers;
	protected InputStream bodyInputStream;
	protected long bodySize;
	
	public HttpResponse() {
		init();
	}
	
	public HttpResponse(int c) {
		init();
		code = c;
	}
	
	public HttpResponse(int c, byte[] b) {
		init();
		code = c;
		setBody(b);
	}
	
	public HttpResponse(int c, String b) {
		init();
		code = c;
		setBody(b);
	}
	
	public void init() {
		code = 200;
		headers = new HashMap<String, String>();
		bodySize = -1;
	}
	
	public void setStatus(int c) {
		code = c;
	}
	
	public int getStatus() {
		return code;
	}
	
	public void setHeader(String key, String value) {
		headers.put(key,  value);
	}
	
	public String getHeader(String name) {
		return headers.get(name);
	}
	
	public Set<String> getHeaderNames() {
		return headers.keySet();
	}
	
	public void setBody(String str) {
		if(str != null) {
			bodyInputStream = new ByteArrayInputStream(str.getBytes());
			bodySize = str.length();
		}
	}
	
	public void setBody(byte[] bytes) {
		if(bytes != null) {
			bodyInputStream = new ByteArrayInputStream(bytes);
			bodySize = bytes.length;
		}
	}
	
	public void setBody(InputStream is) {
		if(is != null) {
			bodyInputStream = is;
			bodySize = 0;
		}
	}
	
	public long getBodySize() {
		return bodySize;
	}
	
	public InputStream getBodyInputStream() {
		return bodyInputStream;
	}
}
