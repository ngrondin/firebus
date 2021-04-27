package io.firebus.interfaces;

import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;

public interface ServiceRequestor 
{
	public void response(Payload payload);

	public void error(FunctionErrorException e);

	public void timeout();
}
