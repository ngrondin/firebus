package io.firebus;

import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.ServiceProvider;

public class SRTest {
	
	public static void main(String[] args) 
	{
		Firebus firebus = new Firebus();
		firebus.registerServiceProvider("testservice", new ServiceProvider() {
			public Payload service(Payload payload) throws FunctionErrorException {
				return new Payload("Hello motto " + payload.getString());
			}

			public ServiceInformation getServiceInformation() {
				return null;
			}
			
		}, 10);
		
		try {
			Payload resp = firebus.requestService("testservice", new Payload("Nicolas"));
			System.out.println("Respionse is : " + resp.getString());
		} catch(Exception e) {
			e.printStackTrace();
		}
		firebus.close();
	}

}
