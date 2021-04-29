package io.firebus.interfaces;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.StreamInformation;

public interface StreamProvider extends BusFunction
{
	public Payload acceptStream(Payload payload, StreamEndpoint streamEndpoint) throws FunctionErrorException;
	
	public int getStreamIdleTimeout();
	
	public StreamInformation getStreamInformation();
}
