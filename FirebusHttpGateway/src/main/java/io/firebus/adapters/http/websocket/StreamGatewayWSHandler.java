package io.firebus.adapters.http.websocket;

import java.util.HashMap;
import java.util.Map;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.adapters.http.WebsocketHandler;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.interfaces.StreamHandler;
import io.firebus.utils.DataMap;

public class StreamGatewayWSHandler extends WebsocketHandler implements StreamHandler {
	protected Map<String, StreamEndpoint> sessionToStream;
	protected Map<StreamEndpoint, String> streamToSession;
	protected String streamName;
	
	public StreamGatewayWSHandler(DataMap c, Firebus f) {
		super(c, f);
		streamName = c.getString("service");
		sessionToStream = new HashMap<String, StreamEndpoint>();
		streamToSession = new HashMap<StreamEndpoint, String>();
	}

	protected void onOpen(String session, Payload payload) throws FunctionErrorException, FunctionTimeoutException {
		StreamEndpoint streamEndpoint = firebus.requestStream(streamName, payload, 10000);
		streamEndpoint.setHandler(this);
		sessionToStream.put(session, streamEndpoint);
		streamToSession.put(streamEndpoint, session);
	}

	protected void onStringMessage(String session, String msg) {
		Payload payload = new Payload(msg);
		sessionToStream.get(session).send(payload);
	}

	protected void onBinaryMessage(String session, byte[] msg) {
		Payload payload = new Payload(msg);
		sessionToStream.get(session).send(payload);
	}

	protected void onClose(String session) {
		StreamEndpoint sep = sessionToStream.get(session);
		if(sep != null) {
			sep.close();
			sessionToStream.remove(session);
			streamToSession.remove(sep);
		}
	}

	public void receiveStreamData(Payload payload, StreamEndpoint streamEndpoint) {
		this.sendStringMessage(streamToSession.get(streamEndpoint), payload.getString());
	}

	public void streamClosed(StreamEndpoint streamEndpoint) {
		close(streamToSession.get(streamEndpoint));
	}

}
