package io.firebus;


import io.firebus.interfaces.Consumer;

public class ConsumerTest {

	public static void main(String[] args) {
		try
		{
			Firebus firebus = new Firebus();
			firebus.registerConsumer("cons1", new Consumer() {
				public void consume(Payload payload) {
					System.out.println("Consumed: " + payload.getString());
				}
			}, 10);
			
			firebus.publish("cons1", new Payload("hello world"));
			Thread.sleep(1000);
			firebus.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		

		
	}
}
