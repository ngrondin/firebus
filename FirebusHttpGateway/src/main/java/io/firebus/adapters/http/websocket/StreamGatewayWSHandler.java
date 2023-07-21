package io.firebus.adapters.http.websocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.adapters.http.WebsocketConnectionHandler;
import io.firebus.adapters.http.WebsocketHandler;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.interfaces.StreamHandler;
import io.firebus.logging.Logger;

public class StreamGatewayWSHandler extends WebsocketConnectionHandler implements StreamHandler {
	protected String streamName;
	protected StreamEndpoint streamEndpoint;
	protected boolean canGZIP = false;
	protected long start = -1;
	protected long lastIn = -1;
	protected long lastOut = -1;
	
	public void configure(WebsocketHandler wsh, HttpServletRequest r) throws ServletException, IOException {
		super.configure(wsh, r);
		streamName = getConfig().getString("service");
		canGZIP = "true".equalsIgnoreCase(parameters.get("wscangzip"));
		start = System.currentTimeMillis();
	}

	protected void onOpen() throws FunctionErrorException, FunctionTimeoutException, ServletException, IOException {
    	Payload payload = getRequestPayload();
    	payload.metadata.put("streamgwnode", String.valueOf(getFirebus().getNodeId()));
    	payload.metadata.put("streamgwid", id);
		String serviceName = handler.getGateway().getServiceName();
		streamEndpoint = getFirebus().requestStream(streamName, payload, serviceName, 10000);
		streamEndpoint.setHandler(this);
		Logger.info("fb.http.ws.streamgw.connected", new DataMap("id", id));
	}

	protected void onStringMessage(String msg) {
		Payload payload = new Payload(msg);
		if(streamEndpoint != null) {
			streamEndpoint.send(payload);
			lastIn = System.currentTimeMillis();
		} else {
			Logger.warning("fb.http.ws.streamgw.receivedbutclosed", new DataMap("id", id));
		}
	}

	protected void onBinaryMessage(byte[] msg) {
		Payload payload = new Payload(msg);
		if(streamEndpoint != null) {
			streamEndpoint.send(payload);
			lastIn = System.currentTimeMillis();
		} else {
			Logger.warning("fb.http.ws.streamgw.receivedbutclosed", new DataMap("id", id));
		}
	}

	protected void onClose() {
		if(streamEndpoint != null) {
			streamEndpoint.close();
		}
		long now = System.currentTimeMillis();
		Logger.info("fb.http.ws.streamgw.closed", new DataMap("id", id, "life", (now - start), "lastin", lastIn > -1 ? (now - lastIn) : -1, "lastout", lastOut > -1 ? (now - lastOut) : -1));
	}

	public void receiveStreamData(Payload payload, StreamEndpoint streamEndpoint) {
		String str = payload.getString();
		if(canGZIP && str.length() > 100000) {
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
			    GZIPOutputStream gzip = new GZIPOutputStream(out);
			    gzip.write(str.getBytes());
			    gzip.close();
			    sendBinaryMessage(out.toByteArray());
			} catch(IOException e) {
				Logger.severe("fb.http.ws.streamgw.receive", e);
			}
		} else {
			sendStringMessage(str);
		}
		lastOut = System.currentTimeMillis();
	}

	public void streamClosed(StreamEndpoint streamEndpoint) {
		destroy();
	}

}
