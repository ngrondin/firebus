package io.firebus.adapters.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.firebus.Payload;
import io.firebus.data.DataMap;

public abstract class SecurityHandler {
	protected HttpGateway httpGateway;
	protected DataMap config;
	protected List<AuthValidationHandler> authValidationHandlers;
	
	public SecurityHandler(HttpGateway gw, DataMap c) 
	{
		httpGateway = gw;
		config = c;
		authValidationHandlers = new ArrayList<AuthValidationHandler>();
	}
	
	public void addAuthValidationHandler(AuthValidationHandler avh)
	{
		authValidationHandlers.add(avh);
	}
	
	protected void unauthenticated(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String accept = req.getHeader("accept");
		String path = req.getRequestURI();
		if(accept != null && (accept.contains("text/html") || accept.contains("*/*"))) {
			if(authValidationHandlers.size() > 1) {
		        PrintWriter writer = resp.getWriter();
		        writer.println("<html><head><title>Login</title><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><style>");
		        writer.println("body{}");
		        writer.println(".main{position:fixed; top:50%; left: 50%; transform: translate(-50%, -50%); font-family:sans-serif; font-size:larger; border:1px solid lightgrey; padding:15px; border-radius:5px;}");
		        writer.println(".title{color:grey; padding:5px;}");
		        writer.println(".option{display:flex; flex-direction:row; align-items:center;padding:5px;}");
		        writer.println("a{display:flex; flex-direction:row; align-items:center;}");
		        writer.println("a:link {color:black; text-decoration:none;} a:visited  {color:black; text-decoration:none;} a:hover {color:black; text-decoration:none;} a:active {color:black; text-decoration:none;}");
		        writer.println("img {padding-right:10px;}");
		        writer.println("</style></head>");
		        writer.println("<body><div class=\"main\"><div class=\"title\">Login with</div>");
		        for(AuthValidationHandler avh: authValidationHandlers) {
			        writer.println("<div class=\"option\"><a href=\"" + avh.getLoginURL(path) + "\"><img src=\"" + avh.getIcon() + "\"><div>" + avh.getLabel() + "</div></a></div>");
		        }
		        writer.println("</div></body></html>");
			} else if(authValidationHandlers.size() == 1) {
				resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
				resp.setHeader("Location", authValidationHandlers.get(0).getLoginURL(path));
			} else {
				resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			}			
		} else {
			resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}

	}
	
	public abstract boolean checkHttpRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
	
	public abstract void enrichFirebusRequest(HttpServletRequest req, Payload payload) throws ServletException, IOException;
	
	public abstract void enrichAuthResponse(String username, HttpServletResponse resp) throws ServletException, IOException;
	
	public abstract void enrichLogoutResponse(HttpServletResponse resp);
}
