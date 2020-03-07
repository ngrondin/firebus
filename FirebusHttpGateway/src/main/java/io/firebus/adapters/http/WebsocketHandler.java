package io.firebus.adapters.http;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.security.ConcurrentMessageDigest;

import io.firebus.Firebus;
import io.firebus.utils.DataMap;

public abstract class WebsocketHandler extends HttpHandler {
	
	private Logger logger = Logger.getLogger("io.firebus.adapters.http");
	
	private static final byte[] WS_ACCEPT = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11".getBytes(StandardCharsets.ISO_8859_1);
	
	protected Map<String, WebsocketConnectionHandler> connections;
	
	public WebsocketHandler(DataMap c, Firebus f) 
	{
		super(c, f);
		connections = new HashMap<String, WebsocketConnectionHandler>();
	}

	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String upgradeHeader = req.getHeader("Upgrade");
		if(upgradeHeader != null && upgradeHeader.equalsIgnoreCase("websocket") && req.getMethod().equals("GET")) {
			String key;
	        key = req.getHeader("Sec-WebSocket-Key");
	        if (key == null) {
	            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
	            return;
	        }

	        // If we got this far, all is good. Accept the connection.
	        resp.setHeader("Upgrade", "websocket");
	        resp.setHeader("Connection", "upgrade");
	        byte[] digest = ConcurrentMessageDigest.digestSHA1(key.getBytes(StandardCharsets.ISO_8859_1), WS_ACCEPT);
	        String acceptKey = Base64.encodeBase64String(digest);
	        resp.setHeader("Sec-WebSocket-Accept", acceptKey);

	        WebsocketConnectionHandler wsHandler = req.upgrade(WebsocketConnectionHandler.class);
	        String sessionId = UUID.randomUUID().toString();
	        wsHandler.setHandler(this);
	        wsHandler.setSessionId(sessionId);
	        connections.put(sessionId, wsHandler);
	        onOpen(sessionId);
		}
	}
	
	public void _onClose(String session) {
		onClose(session);
		connections.remove(session);
	}
	
	public void sendStringMessage(String session, String msg) {
		WebsocketConnectionHandler connection = connections.get(session);
		connection.sendStringMessage(msg);
	}
	
	public void sendBinaryMessage(String session, byte[] bytes) {
		WebsocketConnectionHandler connection = connections.get(session);
		connection.sendBinaryMessage(bytes);
	}
	
	protected abstract void onOpen(String session);
	protected abstract void onStringMessage(String session, String msg);
	protected abstract void onBinaryMessage(String session, byte[] msg);
	protected abstract void onClose(String session);

}
