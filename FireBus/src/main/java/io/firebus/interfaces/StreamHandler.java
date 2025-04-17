package io.firebus.interfaces;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.exceptions.FunctionErrorException;

public interface StreamHandler extends BusFunction
{
	
	public void receiveStreamData(Payload payload);
	
	public void streamClosed();
	
	public void streamError(FunctionErrorException error);

	
}
