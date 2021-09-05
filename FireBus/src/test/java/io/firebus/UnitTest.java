package io.firebus;
	

import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.ServiceProvider;


public class UnitTest
{
	public static void main(String args[])
	{
		try
		{
			Firebus firebus = new Firebus();
			firebus.registerServiceProvider("test", new ServiceProvider() {
				public Payload service(Payload payload) throws FunctionErrorException {
					return new Payload("allo");
				}

				public ServiceInformation getServiceInformation() {
					return null;
				}
				
			}, 10);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
