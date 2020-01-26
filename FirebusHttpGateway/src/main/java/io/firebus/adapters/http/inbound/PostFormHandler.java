package io.firebus.adapters.http.inbound;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.utils.DataException;
import io.firebus.utils.DataMap;

public class PostFormHandler extends InboundHandler 
{
	private static final long serialVersionUID = 1L;

	public PostFormHandler(DataMap c, Firebus f) 
	{
		super(c, f);
	}

	protected Payload processRequest(HttpServletRequest req) throws ServletException, IOException, DataException
	{
		String path = req.getRequestURI();
		String shortPath = path.substring(req.getContextPath().length() + req.getServletPath().length());
		DataMap body = new DataMap();
		Enumeration<String> en = req.getParameterNames();
		while(en.hasMoreElements())
		{
			String key = en.nextElement();
			String val = req.getParameter(key);
			body.put(key, val);
		}
		Payload payload = new Payload(body.toString());
		payload.metadata.put("post", shortPath);
		return payload;
	}

	protected void processResponse(HttpServletResponse resp, Payload payload) throws ServletException, IOException, DataException
	{
		if(payload.metadata.containsKey("mime"))
			resp.setHeader("content-type", payload.metadata.get("mime"));
        PrintWriter writer = resp.getWriter();
        writer.print(payload.getString());
	}	

}
