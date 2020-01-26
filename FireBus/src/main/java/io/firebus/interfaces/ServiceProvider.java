package io.firebus.interfaces;

import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;

public interface ServiceProvider extends BusFunction
{
	public Payload service(Payload payload) throws FunctionErrorException;
	
	public ServiceInformation getServiceInformation();
}
