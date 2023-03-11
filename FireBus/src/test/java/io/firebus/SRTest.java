package io.firebus;



import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.ServiceProvider;

public class SRTest {
	
	public static void main(String[] args) 
	{
		Firebus firebus = new Firebus();
		firebus.registerServiceProvider("thirdservice", new ServiceProvider() {
			public Payload service(Payload payload) throws FunctionErrorException {
				try{Thread.sleep(1000);} catch(Exception e) {}
				return new Payload("Third service " + payload.getString());
			}

			public ServiceInformation getServiceInformation() {
				return null;
			}
		}, 10);

		firebus.registerServiceProvider("secondservice", new ServiceProvider() {
			public Payload service(Payload payload) throws FunctionErrorException {
				String s = "";
				try{
					Payload resp = firebus.requestService("thirdservice", new Payload(payload.getBytes()));
					s = resp.getString();
					//Thread.sleep(1000);
				} catch(Exception e) {}
				return new Payload("Second service " + s);
			}

			public ServiceInformation getServiceInformation() {
				return null;
			}
		}, 10);

	
		firebus.registerServiceProvider("firstservice", new ServiceProvider() {
			public Payload service(Payload payload) throws FunctionErrorException {
				String s = "";
				try{
					Payload resp = firebus.requestService("secondservice", new Payload(payload.getBytes()));
					s = resp.getString();
					//Thread.sleep(1000);
				} catch(Exception e) {}
				return new Payload("First service " + s);
			}

			public ServiceInformation getServiceInformation() {
				return null;
			}
		}, 10);
		
		try {
			Payload resp = firebus.requestService("firstservice", new Payload("Nicolas"));
			System.out.println("Response is : " + resp.getString());
		} catch(Exception e) {
			e.printStackTrace();
		}
		firebus.close();
	}

}
