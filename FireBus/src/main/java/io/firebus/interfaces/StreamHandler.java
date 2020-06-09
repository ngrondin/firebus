package io.firebus.interfaces;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;

public interface StreamHandler extends BusFunction
{
	public void receiveStreamData(Payload payload, StreamEndpoint streamEndpoint);
	
	public void streamClosed(StreamEndpoint streamEndpoint);
	
}
