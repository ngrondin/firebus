package io.firebus.adapters.http.inbound;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.adapters.http.HttpGateway;
import io.firebus.adapters.http.InboundReqRespHandler;
import io.firebus.data.DataException;
import io.firebus.data.DataMap;

public class PostFormHandler extends InboundReqRespHandler 
{
	public PostFormHandler(HttpGateway gw, Firebus f, DataMap c) 
	{
		super(gw, f, c);
	}

	protected Payload processRequest(HttpServletRequest req) throws ServletException, IOException, DataException
	{
		String path = req.getRequestURI();
		String shortPath = path.substring(req.getContextPath().length() + getHttpHandlerPath().length());
		DataMap body = new DataMap();
		Enumeration<String> en = req.getParameterNames();
		while(en.hasMoreElements())
		{
			String key = en.nextElement();
			String val = req.getParameter(key);
			body.put(key, val);
		}
		Payload payload = new Payload(body);
		payload.metadata.put("post", shortPath);
		payload.metadata.put("mime", "application/json");
		return payload;
	}

	protected void processResponse(HttpServletResponse resp, Payload payload) throws ServletException, IOException, DataException
	{
        PrintWriter writer = resp.getWriter();
        writer.print(payload.getString());
	}	

}
