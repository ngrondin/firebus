package com.nic.firebus.adapters.http.inbound;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.utils.DataMap;

public class GetHandler extends InboundHandler 
{
	private static final long serialVersionUID = 1L;

	public GetHandler(DataMap c, Firebus f) 
	{
		super(c, f);
	}

	protected Payload processRequest(HttpServletRequest req) throws ServletException, IOException
	{
		DataMap fbReq = new DataMap();
		String path = req.getRequestURI();
		String shortPath = path.substring(req.getContextPath().length() + req.getServletPath().length());
		fbReq.put("get", shortPath);
		Enumeration<String> en = req.getParameterNames();
		while(en.hasMoreElements())
		{
			String paramName = en.nextElement();
			fbReq.put(paramName, req.getParameter(paramName));
		}
		Payload payload = new Payload(fbReq.toString().getBytes());
		return payload;
	}

	protected void processResponse(HttpServletResponse resp, Payload payload) throws ServletException, IOException
	{
		if(payload.metadata.containsKey("mime"))
			resp.setHeader("content-type", payload.metadata.get("mime"));
        PrintWriter writer = resp.getWriter();
        writer.print(payload.getString());
	}	

}
