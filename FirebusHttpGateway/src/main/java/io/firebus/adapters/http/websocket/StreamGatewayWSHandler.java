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
	protected Map<String, StreamEndpoint> connIdToStream;
	protected Map<StreamEndpoint, String> streamToConnId;
	protected String streamName;
	
	public StreamGatewayWSHandler(DataMap c, Firebus f) {
		super(c, f);
		streamName = c.getString("service");
		connIdToStream = new HashMap<String, StreamEndpoint>();
		streamToConnId = new HashMap<StreamEndpoint, String>();
	}

	protected void onOpen(String connectionId, Payload payload) throws FunctionErrorException, FunctionTimeoutException {
		System.out.println("SGWS opening WS connection " + connectionId); //Temp Logging
		StreamEndpoint streamEndpoint = firebus.requestStream(streamName, payload, 10000);
		connIdToStream.put(connectionId, streamEndpoint);
		streamToConnId.put(streamEndpoint, connectionId);
		streamEndpoint.setHandler(this);
		System.out.println("SGWS opened WS connection " + connectionId); //Temp Logging
	}

	protected void onStringMessage(String connectionId, String msg) {
		Payload payload = new Payload(msg);
		connIdToStream.get(connectionId).send(payload);
	}

	protected void onBinaryMessage(String connectionId, byte[] msg) {
		Payload payload = new Payload(msg);
		connIdToStream.get(connectionId).send(payload);
	}

	protected void onClose(String connectionId) {
		System.out.println("SGWS closing WS connection " + connectionId); //Temp Logging
		StreamEndpoint sep = connIdToStream.get(connectionId);
		if(sep != null) {
			sep.close();
			connIdToStream.remove(connectionId);
			streamToConnId.remove(sep);
		}
		System.out.println("SGWS closed WS connection " + connectionId); //Temp Logging
	}

	public void receiveStreamData(Payload payload, StreamEndpoint streamEndpoint) {
		String connId = streamToConnId.get(streamEndpoint);
		this.sendStringMessage(connId, payload.getString());
		//System.out.println("SGWS sent message to WS connection " + connId + " : " + payload.getString().hashCode()); //Temp Logging
	}

	public void streamClosed(StreamEndpoint streamEndpoint) {
		close(streamToConnId.get(streamEndpoint));
	}

}
