package com.nic.firebus.interfaces;

import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;

public interface ServiceRequestor 
{
	public void requestCallback(Payload payload);

	public void requestErrorCallback(FunctionErrorException e);

	public void requestTimeout();
}
