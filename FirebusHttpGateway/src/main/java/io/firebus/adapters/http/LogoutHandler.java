package io.firebus.adapters.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.firebus.Firebus;
import io.firebus.utils.DataMap;

public class LogoutHandler extends HttpHandler {
	protected List<SecurityHandler> securityHandlers;
		
	public LogoutHandler(DataMap c, Firebus f) {
		super(c, f);
	}
	
	public void setSecuritytHandlers(List<SecurityHandler> sh) {
		securityHandlers = sh;
	}

	protected void httpService(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if(securityHandlers != null) {
			for(SecurityHandler sh: securityHandlers) {
				sh.enrichLogoutResponse(resp);
			}
		}
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
        writer.println("<body><div class=\"main\"><div class=\"title\">Logged out</div>");
        writer.println("</div></body></html>");
		
	}


}
