package io.firebus;


import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.firebus.interfaces.Consumer;

import io.firebus.logging.FirebusSimpleFormatter;

public class ConsumerTest {

	public static void main(String[] args) {
		Logger.getLogger("").removeHandler(Logger.getLogger("").getHandlers()[0]);
		try
		{
			FileHandler fh = new FileHandler("ConsumerTest.log");
			fh.setFormatter(new FirebusSimpleFormatter());
			fh.setLevel(Level.FINEST);
			Logger logger = Logger.getLogger("io.firebus");
			logger.addHandler(fh);
			logger.setLevel(Level.FINEST);
			
			
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
