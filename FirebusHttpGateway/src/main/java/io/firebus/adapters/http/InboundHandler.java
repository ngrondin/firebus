package io.firebus.adapters.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.firebus.Firebus;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.exceptions.FunctionUnavailableException;
import io.firebus.logging.Logger;

public abstract class InboundHandler extends HttpHandler 
{
	
	public InboundHandler(HttpGateway gw, Firebus f, DataMap c) 
	{
		super(gw, f, c);
	}
	
	
	protected void httpService(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		try 
		{
			inboundService(req, resp);
		} 
		catch (SocketException e) 
		{
			// Do nothing, just accept the connection was broken. Adding this here as the below doesn't seem to catch this exception.
		}
		catch (Exception e)
		{
			if(!(e instanceof FunctionErrorException || e instanceof FunctionTimeoutException || e instanceof FunctionUnavailableException))
				Logger.severe("fb.http.inbound", e);
			if(!resp.isCommitted()) {
				int errorCode = (e instanceof FunctionErrorException ? ((FunctionErrorException)e).getErrorCode() : 0);
				resp.reset();
				resp.setStatus(errorCode > 0 ? errorCode : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		        PrintWriter writer = resp.getWriter();
				String accept = req.getHeader("accept");
				if(accept != null && accept.contains("application/json"))
					writer.println("{\r\n\t\"error\" : \"" + e.getMessage().replaceAll("\"", "'").replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n") + "\"\r\n}");
				else
					writer.println("<div>" + e.getMessage() + "</div>");				
			}
		}
	}	
	
	
	public abstract void inboundService(HttpServletRequest req, HttpServletResponse resp) throws Exception;

}
