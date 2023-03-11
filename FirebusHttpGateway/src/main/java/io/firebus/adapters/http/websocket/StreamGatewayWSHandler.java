package io.firebus.adapters.http.websocket;

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
		Logger.warning("fb.http.ws.streamgw.closed", new DataMap("id", id, "life", (now - start), "lastin", lastIn > -1 ? (now - lastIn) : -1, "lastout", lastOut > -1 ? (now - lastOut) : -1));
	}

	public void receiveStreamData(Payload payload, StreamEndpoint streamEndpoint) {
		sendStringMessage(payload.getString());
		lastOut = System.currentTimeMillis();
	}

	public void streamClosed(StreamEndpoint streamEndpoint) {
		destroy();
	}

}
