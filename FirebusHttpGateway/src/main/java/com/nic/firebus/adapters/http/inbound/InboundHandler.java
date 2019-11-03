package com.nic.firebus.adapters.http.inbound;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.adapters.http.Handler;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataMap;

public abstract class InboundHandler extends Handler 
{
	private static final long serialVersionUID = 1L;

	private Firebus firebus;
	private String service;
	
	public InboundHandler(DataMap c, Firebus f) 
	{
		super(c);
		firebus = f;
		service = handlerConfig.getString("service");
	}
	
	
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String token = null;
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
					Payload fbResp = firebus.requestService(service, fbReq);
					processResponse(resp, fbResp);
				}
				else
				{
					resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			        PrintWriter writer = resp.getWriter();
			        writer.println("<html><title>500</title><body>Inbound process failed</body></html>");
				}
			} 
			catch (FunctionErrorException e) 
			{
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		        PrintWriter writer = resp.getWriter();
		        writer.println("<html><title>500</title><body>Firebus function error : " + e.getMessage() + "</body></html>");
			} 
			catch (FunctionTimeoutException e) 
			{
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		        PrintWriter writer = resp.getWriter();
		        writer.println("<html><title>500</title><body>Firebus function timed out</body></html>");
			}
			catch (Exception e)
			{
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		        PrintWriter writer = resp.getWriter();
		        writer.println("<html><title>500</title><body>General Exception : " + e.getMessage() + "</body></html>");
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
