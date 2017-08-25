package com.nic.firebus.interfaces;

import com.nic.firebus.exceptions.FunctionErrorException;

public interface ServiceProvider extends BusFunction
{

	public byte[] service(byte[] payload) throws FunctionErrorException;
	
}
