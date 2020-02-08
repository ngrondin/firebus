package io.firebus.adapters.http;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class MasterHandler extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	
	protected List<HttpHandlerEntry> handlerMap; 
	

	public MasterHandler()
	{
		handlerMap = new ArrayList<HttpHandlerEntry>();
	}
	
	public void addHttpHandler(String path, String method, HttpHandler handler)
	{
		handlerMap.add(new HttpHandlerEntry(path, method, handler));
	}
	

	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String path = req.getRequestURI();
		String method = req.getMethod();
		
		HttpHandler selected = null;
		for(int i = 0; i < handlerMap.size() && selected == null; i++) 
		{
			HttpHandlerEntry entry = handlerMap.get(i);
			boolean match = true;
			if(entry.method.equalsIgnoreCase(method))
			{
				if(entry.path.endsWith("/*")) 
				{
					String shortEntryPath = entry.path.substring(0, entry.path.length() - 2);
					if(path.startsWith(shortEntryPath + "/") || path.equals(shortEntryPath))
						match = true;
					else
						match = false;
				}
				else
				{
					if(entry.path.equals(path))
						match = true;
					else
						match = false;
				}
			}
			else
			{
				match = false;
			}
			if(match)
				selected = entry.handler;
		}
		selected.service(req, resp);
	}	
}
