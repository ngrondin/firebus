package io.firebus.adapters.http;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataMap;

public abstract class InboundReqRespHandler  extends InboundHandler {

	protected String service;
	private int timeout;

	public InboundReqRespHandler(HttpGateway gw, Firebus f, DataMap c) {
		super(gw, f, c);
		timeout = handlerConfig.containsKey("timeout") ? handlerConfig.getNumber("timeout").intValue() : 10000;
		service = handlerConfig.getString("service");	
	}

	public void inboundService(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		Payload fbReq = processRequest(req);
		if(fbReq != null)
		{
			if(securityHandler != null)
				securityHandler.enrichFirebusRequest(req, fbReq);
			enrichFirebusRequestDefault(req, fbReq);
			Payload fbResp = firebus.requestService(service, fbReq, timeout);
			if(fbResp.metadata.containsKey("mime"))
				resp.setHeader("content-type", fbResp.metadata.get("mime"));
			if(fbResp.metadata.containsKey("filename"))
				resp.setHeader("content-disposition", "inline; filename=\"" + fbResp.metadata.get("filename") + "\"");
			if(fbResp.metadata.containsKey("httpcode")) {
				int status = Integer.parseInt(fbResp.metadata.get("httpcode"));
				resp.setStatus(status);
				if(status == 401 && this.securityHandler != null) {
					this.securityHandler.enrichLogoutResponse(resp);
				}
			}
			processResponse(resp, fbResp);
		}
		else
		{
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        PrintWriter writer = resp.getWriter();
	        writer.println("<html><title>500</title><body>Inbound process failed</body></html>");
		}
	}


	protected abstract Payload processRequest(HttpServletRequest req) throws Exception;
	
	protected abstract void processResponse(HttpServletResponse resp, Payload payload) throws Exception;
	

}
