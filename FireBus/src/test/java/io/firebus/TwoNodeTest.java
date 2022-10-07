package io.firebus;

import io.firebus.data.DataException;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.ServiceProvider;

public class TwoNodeTest {

	public static void main(String[] args) 
	{
		Firebus fb1 = new Firebus();
		Firebus fb2 = new Firebus();
		fb2.registerServiceProvider("mapservice", new ServiceProvider() {
			public Payload service(Payload payload) throws FunctionErrorException {
				try {
					DataMap map = payload.getDataMap();
					System.out.println(map.toString());
				} catch (DataException e) {
					e.printStackTrace();
				}
				return new Payload("OK");
			}

			public ServiceInformation getServiceInformation() {
				return null;
			}
		}, 1);
		fb2.registerServiceProvider("stringservice", new ServiceProvider() {
			public Payload service(Payload payload) throws FunctionErrorException {
				try {
					DataMap map = payload.getDataMap();
					System.out.println(map.toString());
				} catch (DataException e) {
					e.printStackTrace();
				}
				return new Payload("OK");
			}

			public ServiceInformation getServiceInformation() {
				return null;
			}
		}, 1);
		
		try {
			fb1.requestService("stringservice", new Payload(new DataMap("request", "hello").toString()));
			fb1.requestService("mapservice", new Payload(new DataMap("request", "hello")));
			fb1.close();
			fb2.close();
		} catch (FunctionErrorException | FunctionTimeoutException e) {
			e.printStackTrace();
		}
	}
}
