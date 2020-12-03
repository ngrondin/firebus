package io.firebus.adapters.http;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class MasterHandler extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	
	protected List<HttpHandlerEntry> handlerMap; 
	protected HttpHandler defaultHandler;
	protected HttpHandler logoutHandler;
	protected String rootForward;

	public MasterHandler()
	{
		handlerMap = new ArrayList<HttpHandlerEntry>();
		rootForward = null;
	}
	
	public void addHttpHandler(String path, String method, HttpHandler handler)
	{
		handlerMap.add(new HttpHandlerEntry(path, method, handler));
	}
	
	public void setDefaultHander(HttpHandler dh)
	{
		defaultHandler = dh;
	}
	
	public void setLogouHander(HttpHandler lh)
	{
		logoutHandler = lh;
	}
	
	public void setRootForward(String path)
	{
		rootForward = path;
	}
	

	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String path = req.getRequestURI();
		String method = req.getMethod();
		
		if(path.equals("/") && rootForward != null)
			path = rootForward;

		HttpHandler selected = null;
		if(path.equals("/logout")) 
		{
			selected = logoutHandler;
		}
		else 
		{
			for(int i = 0; i < handlerMap.size() && selected == null; i++) 
			{
				HttpHandlerEntry entry = handlerMap.get(i);
				boolean match = true;
				if(entry.method == null || (entry.method != null && entry.method.equalsIgnoreCase(method)))
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
		}

		if(selected != null)
		{
			selected.service(req, resp);
		}
		else if(defaultHandler != null)
		{
			defaultHandler.service(req, resp);
		}
		else
		{
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
	        PrintWriter writer = resp.getWriter();
			writer.println("No mapping.");
		}			
	}	
}
