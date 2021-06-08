package io.firebus.adapters.http;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.security.ConcurrentMessageDigest;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.utils.DataMap;

public abstract class WebsocketHandler extends HttpHandler {
	
	private Logger logger = Logger.getLogger("io.firebus.adapters.http");
	
	private static final byte[] WS_ACCEPT = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11".getBytes(StandardCharsets.ISO_8859_1);
	
	protected Map<String, WebsocketConnectionHandler> connections;
	
	public WebsocketHandler(HttpGateway gw, Firebus f, DataMap c) 
	{
		super(gw, f, c);
		connections = new HashMap<String, WebsocketConnectionHandler>();
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

	        WebsocketConnectionHandler wsConnHandler = req.upgrade(WebsocketConnectionHandler.class);
	        wsConnHandler.setHandler(this);
        	Payload payload = new Payload();
        	if(securityHandler != null)
        		securityHandler.enrichFirebusRequest(req, payload);
        	enrichFirebusRequestDefault(req, payload);
        	wsConnHandler.setRequestPayload(payload);
	        connections.put(wsConnHandler.getConnectionId(), wsConnHandler);
		}
	}
	
	protected void _onOpen(String connectionId) {
        try {
        	WebsocketConnectionHandler connection = connections.get(connectionId);
        	onOpen(connectionId, connection.getRequestPayload());
        } catch(Exception e) {
        	logger.severe("Error openning WS connection: " + e.getMessage());
        }		
	}
	
	public void _onClose(String connectionId) {
		try {
			onClose(connectionId);
		} catch(Exception e) {
			logger.severe("Error closing WS connection: " + e.getMessage());
		}
		connections.remove(connectionId);
	}
	
	public void sendStringMessage(String connectionId, String msg) {
		WebsocketConnectionHandler connection = connections.get(connectionId);
		connection.sendStringMessage(msg);
	}
	
	public void sendBinaryMessage(String connectionId, byte[] bytes) {
		WebsocketConnectionHandler connection = connections.get(connectionId);
		connection.sendBinaryMessage(bytes);
	}
	
	public void close(String connectionId) {
		WebsocketConnectionHandler connection = connections.get(connectionId);
		if(connection != null)
			connection.destroy();
	}
	
	protected abstract void onOpen(String connectionId, Payload payload) throws FunctionErrorException, FunctionTimeoutException;
	protected abstract void onStringMessage(String connectionId, String msg) throws FunctionErrorException, FunctionTimeoutException;
	protected abstract void onBinaryMessage(String connectionId, byte[] msg) throws FunctionErrorException, FunctionTimeoutException;
	protected abstract void onClose(String connectionId) throws FunctionErrorException, FunctionTimeoutException;

}
