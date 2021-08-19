package io.firebus.adapters.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.firebus.Firebus;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.exceptions.FunctionUnavailableException;
import io.firebus.utils.DataMap;
import io.firebus.utils.StackUtils;

public abstract class InboundHandler extends HttpHandler 
{
	private Logger logger = Logger.getLogger("io.firebus.adapters.http");
	
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
		catch (Exception e)
		{
			if(!(e instanceof FunctionErrorException || e instanceof FunctionTimeoutException || e instanceof FunctionUnavailableException))
				logger.severe("Error processing inbound : " + e.getMessage() + "\r\n" + StackUtils.toString(e.getStackTrace()));
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
	
	
	public abstract void inboundService(HttpServletRequest req, HttpServletResponse resp) throws Exception;

}
