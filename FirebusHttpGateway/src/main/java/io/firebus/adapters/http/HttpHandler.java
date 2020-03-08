package io.firebus.adapters.http;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.firebus.Firebus;
import io.firebus.utils.DataMap;

public abstract class HttpHandler extends Handler 
{

	public HttpHandler(DataMap c, Firebus fb) 
	{
		super(c, fb);
	}

	protected String getHttpHandlerPath() 
	{
		String path = handlerConfig.getString("path");
		if(path.endsWith("/*"))
			path = path.substring(0, path.length() - 2);
		return path;
	}
	
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String token = null;
		resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Cache-Control", "no-cache");

		if(req.getMethod().equals("OPTIONS"))
		{
			resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
			resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
			resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
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
				httpService(token, req, resp);
			}
		}		
	}
	
	protected abstract void httpService(String token, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
	
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
