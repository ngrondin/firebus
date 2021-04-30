package io.firebus.utils;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.interfaces.StreamHandler;

public class StreamPipe implements StreamHandler {
	protected StreamEndpoint streamEndpoint1;
	protected StreamEndpoint streamEndpoint2;
	
	public StreamPipe(StreamEndpoint sep1, StreamEndpoint sep2) {
		streamEndpoint1 = sep1;
		streamEndpoint2 = sep2;
		streamEndpoint1.setHandler(this);
		streamEndpoint2.setHandler(this);
	}

	public void receiveStreamData(Payload payload, StreamEndpoint streamEndpoint) {
		if(streamEndpoint == streamEndpoint1)
			streamEndpoint2.send(payload);
		else
			streamEndpoint1.send(payload);
	}

	public void streamClosed(StreamEndpoint streamEndpoint) {
		streamEndpoint1.close();
		streamEndpoint2.close();
	}
}
