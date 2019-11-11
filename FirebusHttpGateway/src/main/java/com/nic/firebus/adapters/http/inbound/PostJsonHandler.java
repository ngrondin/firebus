package com.nic.firebus.adapters.http.inbound;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataMap;

public class PostJsonHandler extends InboundHandler 
{
	private static final long serialVersionUID = 1L;

	public PostJsonHandler(DataMap c, Firebus f) 
	{
		super(c, f);
	}

	protected Payload processRequest(HttpServletRequest req) throws ServletException, IOException, DataException
	{
		String path = req.getRequestURI();
		String shortPath = path.substring(req.getContextPath().length() + req.getServletPath().length());
		DataMap body = new DataMap(req.getInputStream());
		Payload payload = new Payload(body.toString());
		payload.metadata.put("put", shortPath);
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
