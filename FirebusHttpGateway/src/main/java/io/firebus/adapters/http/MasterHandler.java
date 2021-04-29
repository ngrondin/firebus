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
	
	public void addHttpHandler(String path, String method, String contentType, HttpHandler handler)
	{
		handlerMap.add(new HttpHandlerEntry(path, method, contentType, handler));
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
		String contentType = req.getContentType();
		
		if(path.equals("/") && rootForward != null)
			path = rootForward;

		HttpHandler best = null;
		int bestPoints = 0;

		if(path.equals("/logout")) 
		{
			best = logoutHandler;
		}
		else 
		{
			for(int i = 0; i < handlerMap.size() && best == null; i++) 
			{
				HttpHandlerEntry entry = handlerMap.get(i);
				int matchPoints = 0;
				boolean disqualified = false;
				if(entry.path != null) {
					if(entry.path.endsWith("/*")) 
					{
						String shortEntryPath = entry.path.substring(0, entry.path.length() - 2);
						if(path.startsWith(shortEntryPath + "/") || path.equals(shortEntryPath))
							matchPoints++;
						else
							disqualified = true;
					}
					else
					{
						if(entry.path.equals(path))
							matchPoints++;
						else
							disqualified = true;
					}					
				} 
				
				if(entry.method != null) {
					if(entry.method.equalsIgnoreCase(method))
						matchPoints++;
					else
						disqualified = true;
				} 
				
				if(entry.contentType != null) {
					if(entry.contentType.equalsIgnoreCase(contentType))
						matchPoints++;
					else
						disqualified = true;
				} 
				
				if(!disqualified && matchPoints > bestPoints) {
					bestPoints = matchPoints;
					best = entry.handler;
				}
				
			}			
		}

		if(best != null)
		{
			best.service(req, resp);
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
