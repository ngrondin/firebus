package io.firebus.adapters.http.websocket;

import java.util.logging.Logger;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.adapters.http.WebsocketConnectionHandler;
import io.firebus.adapters.http.WebsocketHandler;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.interfaces.StreamHandler;

public class StreamGatewayWSHandler extends WebsocketConnectionHandler implements StreamHandler {
	private Logger logger = Logger.getLogger("io.firebus.adapters.http");
	protected String streamName;
	protected StreamEndpoint streamEndpoint;
	protected long start = -1;
	protected long lastIn = -1;
	protected long lastOut = -1;
	
	public void configure(WebsocketHandler wsh, Payload p) {
		super.configure(wsh, p);
		streamName = getConfig().getString("service");
		start = System.currentTimeMillis();
	}

	protected void onOpen() throws FunctionErrorException, FunctionTimeoutException {
		requestPayload.metadata.put("streamgwnode", String.valueOf(getFirebus().getNodeId()));
		requestPayload.metadata.put("streamgwid", id);
		String serviceName = handler.getGateway().getServiceName();
		streamEndpoint = getFirebus().requestStream(streamName, requestPayload, serviceName, 10000);
		streamEndpoint.setHandler(this);
		logger.info("Stream gateway connection " + id + " opened");
	}

	protected void onStringMessage(String msg) {
		Payload payload = new Payload(msg);
		if(streamEndpoint != null) {
			streamEndpoint.send(payload);
			lastIn = System.currentTimeMillis();
		} else {
			logger.warning("Stream Gateway received string message but stream endpoint was closed");
		}
	}

	protected void onBinaryMessage(byte[] msg) {
		Payload payload = new Payload(msg);
		if(streamEndpoint != null) {
			streamEndpoint.send(payload);
			lastIn = System.currentTimeMillis();
		} else {
			logger.warning("Stream Gateway received binary message but stream endpoint was closed");
		}
	}

	protected void onClose() {
		if(streamEndpoint != null) {
			streamEndpoint.close();
		}
		long now = System.currentTimeMillis();
		logger.info("Stream gateway connection " + id + " closed (life: " + (now - start) + "ms  last_in: " + (lastIn > -1 ? (now - lastIn) + "ms" : "-") + "  last_out: " + (lastOut > -1 ? (now - lastOut) + "ms" : "-") + ")");
	}

	public void receiveStreamData(Payload payload, StreamEndpoint streamEndpoint) {
		sendStringMessage(payload.getString());
		lastOut = System.currentTimeMillis();
	}

	public void streamClosed(StreamEndpoint streamEndpoint) {
		destroy();
	}

}
