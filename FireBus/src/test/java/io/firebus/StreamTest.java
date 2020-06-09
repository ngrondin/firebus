package io.firebus;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.information.StreamInformation;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.interfaces.StreamHandler;
import io.firebus.interfaces.StreamProvider;
import io.firebus.logging.FirebusSimpleFormatter;

public class StreamTest {
	
	public static void main(String[] args) 
	{
		Logger.getLogger("").removeHandler(Logger.getLogger("").getHandlers()[0]);
		try
		{
			FileHandler fh = new FileHandler("StreamTest.log");
			fh.setFormatter(new FirebusSimpleFormatter());
			fh.setLevel(Level.FINEST);
			Logger logger = Logger.getLogger("io.firebus");
			logger.addHandler(fh);
			logger.setLevel(Level.FINEST);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
		Firebus firebus = new Firebus();
		firebus.registerStreamProvider("teststream", new StreamProvider() {
			public void acceptStream(Payload payload, StreamEndpoint streamEndpoint) throws FunctionErrorException {
				System.out.println("Provider end : Accepting : " + payload.getString());
				streamEndpoint.setHandler(new StreamHandler() {
					public void receiveStreamData(Payload payload, StreamEndpoint streamEndpoint) {
						System.out.println("Provider end : received : " + payload.getString());
						streamEndpoint.send(new Payload("Here's a response to : " + payload.getString()));
					}

					public void streamTimeout(StreamEndpoint streamEndpoint) {
						System.out.println("Provider end : Stream has timed out");
					}					
				});
			}

			public StreamInformation getStreamInformation() {
				return null;
			}
			
		}, 10);
		
		try {
			StreamEndpoint streamEndpoint = firebus.requestStream("teststream", new Payload("Please let me in"), 2000);
			streamEndpoint.setHandler(new StreamHandler() {
				public void receiveStreamData(Payload payload, StreamEndpoint streamEndpoint) {
					System.out.println("Requestor end: received : " + payload.getString());
				}

				public void streamTimeout(StreamEndpoint streamEndpoint) {
					System.out.println("Requestor end: Stream has timed out");
				}
				
			});
			System.out.println("Stream established");
			//Thread.sleep(1000);
			streamEndpoint.send(new Payload("Nicolas"));
			//Thread.sleep(1000);
			streamEndpoint.send(new Payload("Nicolas Again"));
			Thread.sleep(1000);
		} catch(Exception e) {
			e.printStackTrace();
		}
		firebus.close();
	}

}
