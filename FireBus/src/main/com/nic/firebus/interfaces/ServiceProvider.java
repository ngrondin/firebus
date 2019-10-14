package com.nic.firebus.interfaces;

import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;

public interface ServiceProvider extends BusFunction
{
	public Payload service(Payload payload) throws FunctionErrorException;
	
	public ServiceInformation getServiceInformation();
}
