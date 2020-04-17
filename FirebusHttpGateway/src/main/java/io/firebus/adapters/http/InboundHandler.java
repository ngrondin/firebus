package io.firebus.adapters.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.utils.DataException;
import io.firebus.utils.DataMap;

public abstract class InboundHandler extends HttpHandler 
{
	private Logger logger = Logger.getLogger("io.firebus.adapters.http");
	
	private String service;
	private int timeout;
	
	public InboundHandler(DataMap c, Firebus f) 
	{
		super(c, f);
		service = handlerConfig.getString("service");
		timeout = handlerConfig.containsKey("timeout") ? handlerConfig.getNumber("timeout").intValue() : 10000;
	}
	
	
	protected void httpService(String token, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
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
			String accept = req.getHeader("accept");
			if(accept != null && accept.contains("application/json"))
				writer.println("{\r\n\t\"error\" : \"" + e.getMessage().replaceAll("\"", "'").replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n") + "\"\r\n}");
			else
				writer.println("<div>" + e.getMessage() + "</div>");
		}
	}	
	
	
	protected abstract Payload processRequest(HttpServletRequest req) throws ServletException, IOException, DataException;
	
	protected abstract void processResponse(HttpServletResponse resp, Payload payload) throws ServletException, IOException, DataException;
	


}
