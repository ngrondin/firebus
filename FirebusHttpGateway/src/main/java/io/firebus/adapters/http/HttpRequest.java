package io.firebus.adapters.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.sun.net.httpserver.Headers;

@SuppressWarnings("restriction")
public class HttpRequest {
	protected String method;
	protected URI uri;
	protected String fullpath;
	protected String contextpath;
	protected String shortpath;
	protected Headers headers;
	protected Map<String, String> parameters;
	protected Map<String, Object> security;
	protected InputStream bodyInputStream;
	
	public HttpRequest(String m, URI u, Headers h, InputStream is) {
		init(m, u, h, is, null);
	}
	
	public HttpRequest(String m, URI u, Headers h, InputStream is, String cp) {
		init(m, u, h, is, cp);
	}
	
	public void init(String m, URI u, Headers h, InputStream is, String cp) {
		method = m != null ? m : "GET";
		uri = u;
		headers = h != null ? h : new Headers();
		bodyInputStream = is;
		parameters = new HashMap<String, String>();
		security = new HashMap<String, Object>();
		contextpath = cp != null ? cp : "/";
		fullpath = uri.getPath();
		shortpath = fullpath.substring(contextpath.length());
		String query = uri.getQuery();
		if(query != null) {
			String[] qparts = query.split("&");
			for(String part : qparts) {
				String[] subparts = part.split("=");
				parameters.put(subparts[0],  subparts[1]);
			}			
		}
	}
	
	
	public String getMethod() {
		return method;
	}
	
	public String getShortPath() {
		return shortpath;
	}
	
	public String getFullPath() {
		return shortpath;
	}
	
	public String getParameter(String key) {
		return parameters.get(key);
	}
	
	public Set<String> getParameterNames() {
		return parameters.keySet();
	}
	
	public List<String> getHeader(String key) {
		return headers.get(key);
	}
	
	public String getHeaderFirstValue(String key) {
		List<String> values = headers.get(key);
		if(values != null && values.size() > 0) 
			return values.get(0);
		else
			return null;
	}
	
	public boolean accepts(String mime) {
		List<String> lines = headers.get("Accept");
		if(lines == null || (lines != null && lines.size() == 0)) {
			return true;
		} else {
			for(String line : lines) {
				String[] parts = line.split(",");
				for(String part : parts) {
					String value = part.trim();
					if(value.equals("*/*") || value.equals(mime)) 
						return true;
				}
			}
		}
		return false;
	}
	
	public Set<String> getHeaderNames() {
		return headers.keySet();
	}
	
	public InputStream getBodyInputStream() {
		return bodyInputStream;
	}
	
	public byte[] readEntireBody() throws IOException {
		if(bodyInputStream != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    int read;
		    byte[] data = new byte[1024];
		    while ((read = bodyInputStream.read(data, 0, data.length)) != -1)
		    	baos.write(data, 0, read);
		    baos.flush();
		    return baos.toByteArray();
		} else {
			return null;
		}
	}
	
	public void setSecurityData(String key, Object value) {
		security.put(key, value);
	}
	
	public Object getSecurityData(String key) {
		return security.get(key);
	}
}
