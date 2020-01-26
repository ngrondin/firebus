package io.firebus.interfaces;

import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;

public interface ServiceRequestor 
{
	public void requestCallback(Payload payload);

	public void requestErrorCallback(FunctionErrorException e);

	public void requestTimeout();
}
