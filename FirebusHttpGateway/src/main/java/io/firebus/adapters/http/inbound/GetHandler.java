package io.firebus.adapters.http.inbound;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.adapters.http.InboundHandler;
import io.firebus.utils.DataMap;

public class GetHandler extends InboundHandler 
{
	public GetHandler(DataMap c, Firebus f) 
	{
		super(c, f);
	}

	protected Payload processRequest(HttpServletRequest req) throws ServletException, IOException
	{
		DataMap fbReq = new DataMap();
		String path = req.getRequestURI();
		int handlerPathLen = req.getContextPath().length() + getHttpHandlerPath().length();
		String shortPath = handlerPathLen <= path.length() ? path.substring(handlerPathLen) : "/";
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
		OutputStream os = resp.getOutputStream();
		os.write(payload.getBytes());
		os.flush();
		os.close();
	}	

}
