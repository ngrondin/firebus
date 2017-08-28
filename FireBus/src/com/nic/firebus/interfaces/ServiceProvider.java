package com.nic.firebus.interfaces;

import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;

public interface ServiceProvider extends BusFunction
{
	public Payload service(Payload payload) throws FunctionErrorException;
}
