package io.firebus.interfaces;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.StreamInformation;

public interface StreamProvider extends BusFunction
{
	public void acceptStream(Payload payload, StreamEndpoint streamEndpoint) throws FunctionErrorException;
	
	public StreamInformation getStreamInformation();
}
