package io.firebus.adapters.http.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.adapters.http.HttpRequest;
import io.firebus.adapters.http.HttpResponse;
import io.firebus.adapters.http.Tools;
import io.firebus.data.DataMap;

public abstract class ReqRespHandler  extends InboundHandler {
	private Logger logger = Logger.getLogger("io.firebus.adapters.http");

	protected String service;
	private int timeout;

	public ReqRespHandler(Firebus f, DataMap c) {
		super(f, c);
		timeout = handlerConfig.containsKey("timeout") ? handlerConfig.getNumber("timeout").intValue() : 10000;
		service = handlerConfig.getString("service");	
	}

	public HttpResponse httpService(HttpRequest req) throws Exception {
		HttpResponse httpResp = null;
		Payload fbReq = produceFirebusRequest(req);
		if(fbReq != null)
		{
			if(securityHandler != null)
				securityHandler.enrichFirebusRequest(req, fbReq);
			Tools.enrichFirebusRequestDefault(req, fbReq);
			logger.finest(fbReq.toString());
			Payload fbResp = firebus.requestService(service, fbReq, timeout);
			if(logger.getLevel() == Level.FINEST)
				logger.finest(fbResp.toString());
			httpResp = produceHttpResponse(fbResp);
			if(fbResp.metadata.containsKey("mime"))
				httpResp.setHeader("content-type", fbResp.metadata.get("mime"));
			if(fbResp.metadata.containsKey("filename"))
				httpResp.setHeader("content-disposition", "inline; filename=\"" + fbResp.metadata.get("filename") + "\"");
			if(fbResp.metadata.containsKey("httpcode")) {
				int status = Integer.parseInt(fbResp.metadata.get("httpcode"));
				httpResp.setStatus(status);
				if(status == 401 && securityHandler != null) {
					securityHandler.enrichLogoutResponse(httpResp);
				}
			}
		}
		else
		{
			httpResp = new HttpResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        httpResp.setBody("<html><title>500</title><body>Inbound process failed</body></html>");
		}
		return httpResp;
	}


	protected abstract Payload produceFirebusRequest(HttpRequest req) throws Exception;
	
	protected abstract HttpResponse produceHttpResponse(Payload payload) throws Exception;
	

}
