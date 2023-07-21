package io.firebus.adapters.http;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.security.ConcurrentMessageDigest;

import io.firebus.Firebus;
import io.firebus.data.DataMap;

public class WebsocketHandler extends HttpHandler {
	
	private static final byte[] WS_ACCEPT = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11".getBytes(StandardCharsets.ISO_8859_1);
	
	protected Class<WebsocketConnectionHandler> connHandlerClass;
	
	@SuppressWarnings("unchecked")
	public WebsocketHandler(HttpGateway gw, Firebus f, DataMap c, Class<?> clz) 
	{
		super(gw, f, c);
		connHandlerClass = (Class<WebsocketConnectionHandler>) clz;
	}

	protected void httpService(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String upgradeHeader = req.getHeader("Upgrade");
		if(upgradeHeader != null && upgradeHeader.equalsIgnoreCase("websocket") && req.getMethod().equals("GET")) {
			String key;
	        key = req.getHeader("Sec-WebSocket-Key");
	        if (key == null) {
	            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
	            return;
	        }

	        resp.setHeader("Upgrade", "websocket");
	        resp.setHeader("Connection", "upgrade");
	        byte[] digest = ConcurrentMessageDigest.digestSHA1(key.getBytes(StandardCharsets.ISO_8859_1), WS_ACCEPT);
	        String acceptKey = Base64.encodeBase64String(digest);
	        resp.setHeader("Sec-WebSocket-Accept", acceptKey);

	        WebsocketConnectionHandler wsConnHandler = req.upgrade(connHandlerClass);
        	wsConnHandler.configure(this, req);
		}
	}
	
	public HttpGateway getGateway() 
	{
		return httpGateway;
	}
	
}
