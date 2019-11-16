package com.nic.firebus.adapters.http.inbound;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.adapters.http.Handler;
import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataMap;

public abstract class InboundHandler extends Handler 
{
	private static final long serialVersionUID = 1L;
	private Logger logger = Logger.getLogger("com.nic.firebus.adapters.http");
	
	private Firebus firebus;
	private String service;
	private int timeout;
	
	public InboundHandler(DataMap c, Firebus f) 
	{
		super(c);
		firebus = f;
		service = handlerConfig.getString("service");
		timeout = handlerConfig.containsKey("timeout") ? handlerConfig.getNumber("timeout").intValue() : 10000;
	}
	
	
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String token = null;
		String accept = req.getHeader("accept");
		if(req.getMethod().equals("OPTIONS"))
		{
			resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
			resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
			resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
			resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
			resp.setHeader("Access-Control-Allow-Credentials", "true");
		}
		else
		{
			if(handlerConfig.containsKey("authentication")  &&  (token = getToken(req)) == null)
			{
				DataMap authConfig = handlerConfig.getObject("authentication");
				String loginUrl = authConfig.getString("loginurl");
				if(loginUrl != null)
				{
					String currentUrl = req.getRequestURL().toString();
					String path = req.getRequestURI();
					String host = currentUrl.substring(0, (currentUrl.length() - path.length()));
					String nonce = "12345";
					String resolvedLoginUrl = loginUrl.replace("${host}", host).replace("${path}", path).replace("${currenturl}", currentUrl).replace("${nonce}", nonce);
					resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
					resp.setHeader("Location", resolvedLoginUrl);
				}
				else
				{
					resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			        PrintWriter writer = resp.getWriter();
			        writer.println("<html><title>401</title><body>Unauthorised</body></html>");
				}	
			}
			else
			{
				try 
				{
					Payload fbReq = processRequest(req);
					if(fbReq != null)
					{
						if(token != null)
							fbReq.metadata.put("token", token);
						logger.finest(fbReq.toString());
						Payload fbResp = firebus.requestService(service, fbReq, timeout);
						logger.finest(fbResp.toString());
						resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
						resp.setHeader("Access-Control-Allow-Credentials", "true");
						processResponse(resp, fbResp);
					}
					else
					{
						resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				        PrintWriter writer = resp.getWriter();
				        writer.println("<html><title>500</title><body>Inbound process failed</body></html>");
					}
				} 
				catch (Exception e)
				{
					logger.severe("Error processing inbound : " + e.getMessage());
					resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			        PrintWriter writer = resp.getWriter();
					if(accept.contains("application/json"))
						writer.println("{\r\n\t\"error\" : \"" + e.getMessage().replaceAll("\"", "'").replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n") + "\"\r\n}");
					if(accept.contains("text/html"))
						writer.println("<div>" + e.getMessage() + "</div>");
				}
			}
		}
	}	
	
	protected abstract Payload processRequest(HttpServletRequest req) throws ServletException, IOException, DataException;
	
	protected abstract void processResponse(HttpServletResponse resp, Payload payload) throws ServletException, IOException, DataException;
	
	protected String getToken(HttpServletRequest req)
	{
		String token = null;
		if(handlerConfig.containsKey("authentication"))
		{
			DataMap authConfig = handlerConfig.getObject("authentication");
			String type = authConfig.getString("type");
			if(type.equals("bearer"))
			{
				String authHeader = req.getHeader("Authorization");
				if(authHeader != null && authHeader.startsWith("Bearer"))
					token = authHeader.substring(6).trim();
			}
			else if(type.equals("cookie"))
			{
				String cookieName = authConfig.getString("cookie");
				if(cookieName != null)
				{
					Cookie[] cookies = req.getCookies();
					if(cookies != null)
						for (int i = 0; i < cookies.length; i++) 
							if(cookies[i].getName().equals(cookieName))
								token = cookies[i].getValue();
				}
			}		
		}
		return token;
	}

}
