package io.firebus.utils;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.interfaces.StreamHandler;

public class StreamPipe implements StreamHandler {
	protected StreamEndpoint targetEndPoint;
	protected StreamEndpoint sourceEndPoint;
	
	public StreamPipe(StreamEndpoint t, StreamEndpoint s) {
		targetEndPoint = t;
		sourceEndPoint = s;
		sourceEndPoint.setHandler(this);
		
	}

	public void receiveStreamData(Payload payload) {
		targetEndPoint.send(payload);
	}

	public void streamClosed() {
		targetEndPoint.close();
	}

	public void streamError(FunctionErrorException error) {
		targetEndPoint.error(error);
	}
}
