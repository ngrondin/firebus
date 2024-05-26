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
	protected List<IDMHandler> idmHandlers;
	protected List<String> usersToLogout;
	
	public SecurityHandler(HttpGateway gw, DataMap c)  {
		httpGateway = gw;
		config = c;
		idmHandlers = new ArrayList<IDMHandler>();
		usersToLogout = new ArrayList<String>();
	}
	
	public void addIDMHandler(IDMHandler avh) {
		idmHandlers.add(avh);
	}
	
	protected IDMHandler getIDMHandler(String uri) {
		for(IDMHandler idm: idmHandlers) {
			if(uri.equals(idm.getUri()))
				return idm;
		}
		return null;
	}
	
	protected boolean acceptsFirst(HttpServletRequest req, String mime) {
		String acceptString = req.getHeader("accept");
		String[] parts = acceptString != null ? acceptString.split(",") : null;
		return parts != null && parts.length > 0 && parts[0].equalsIgnoreCase(mime) ? true : false;
	}
	
	protected void sendUnauthenticatedResponse(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = req.getRequestURI();
		if(acceptsFirst(req, "text/html")) {
			if(idmHandlers.size() > 1) {
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
		        for(IDMHandler avh: idmHandlers) {
			        writer.println("<div class=\"option\"><a href=\"" + avh.getLoginURL(path) + "\"><img src=\"" + avh.getIcon() + "\"><div>" + avh.getLabel() + "</div></a></div>");
		        }
		        writer.println("</div></body></html>");
			} else if(idmHandlers.size() == 1) {
				resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
				resp.setHeader("Location", idmHandlers.get(0).getLoginURL(path));
			} else {
				resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			}			
		} else {
			resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}
	
	protected void sendNeedToRefreshResponse(HttpServletRequest req, HttpServletResponse resp, String issuer) throws ServletException, IOException {
		IDMHandler idm = getIDMHandler(issuer);
		if(acceptsFirst(req, "text/html")) {
			if(idm != null) {
				resp.sendRedirect(idm.getRefreshUrl(req.getRequestURI()));
			} else {
				resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			}			
		} else {
			resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}
	
	public void logoutUser(String username) {
		usersToLogout.add(username);
	}
	
	public abstract boolean checkHttpRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
	
	public abstract String extractRefreshToken(HttpServletRequest req) throws ServletException, IOException;
	
	public abstract void enrichFirebusRequest(HttpServletRequest req, Payload payload) throws ServletException, IOException;
	
	public abstract void enrichAuthResponse(HttpServletRequest req, HttpServletResponse resp, String accessToken, long expiry, String refreshToken, String refreshPath, String state) throws ServletException, IOException;
	
	public abstract void enrichRefreshResponse(HttpServletRequest req, HttpServletResponse resp, String accessToken, long expiry, String refreshToken, String refreshPath, String state) throws ServletException, IOException;
	
	public abstract void enrichLogoutResponse(HttpServletRequest req, HttpServletResponse resp);
}
