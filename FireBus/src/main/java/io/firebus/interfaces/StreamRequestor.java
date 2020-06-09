package io.firebus.interfaces;

import io.firebus.StreamEndpoint;
import io.firebus.exceptions.FunctionErrorException;

public interface StreamRequestor 
{
	public void initiateCallback(StreamEndpoint stream);

	public void initiateErrorCallback(FunctionErrorException e);

	public void initiateTimeout();
	
}
