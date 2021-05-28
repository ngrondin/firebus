package io.firebus;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.logging.FirebusSimpleFormatter;

public class LoadTest {
	
	public class Requestor extends Thread {
		protected Firebus firebus;
		public boolean isRunning = true;
		protected int count = 20;
		
		public Requestor(Firebus fb, int c) {
			firebus = fb;
			count = c;
			start();
		}
		
		public void run() {
			for(int i = 0; i < count; i++) {
				try {
					Payload resp = firebus.requestService("service", new Payload("request"));
					System.out.println("recieved");
					Thread.sleep(50);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			isRunning = false;
		}
	}

	public LoadTest() {
		try {
			Firebus firebus = new Firebus();
			firebus.setThreadCount(30);
			Thread.sleep(1000);
			firebus.registerServiceProvider("service", new ServiceProvider() {

				public Payload service(Payload payload) throws FunctionErrorException {
					try { Thread.sleep(200); } catch(Exception e) {}
					return new Payload("response");
				}

				public ServiceInformation getServiceInformation() {
					return null;
				}}, 10);
			
			int iterations = 1;
			int threads = 20;
			Requestor[] list = new Requestor[threads];
			long start = System.currentTimeMillis();
			for(int i = 0; i < list.length; i++)
				list[i] = new Requestor(firebus, iterations);
			
			boolean allDone;
			do {
				allDone = true;
				Thread.sleep(100);
				for(int i = 0; i < list.length; i++) 
					if(list[i].isRunning == true)
						allDone = false;
			} while(allDone == false);
			
			long end = System.currentTimeMillis();
			long totalDuration = (end - start);
			long singleDuration = totalDuration / (iterations * threads);
			System.out.println("Done in " + totalDuration + "ms at " + singleDuration + "ms per unit");
			firebus.close();
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	
	public static void main(String[] args) {
		Logger.getLogger("").removeHandler(Logger.getLogger("").getHandlers()[0]);
		try
		{
			Level lvl = Level.INFO;
			//FileHandler fh = new FileHandler("LoadTest.log");
			//fh.setFormatter(new FirebusSimpleFormatter());
			//fh.setLevel(lvl);
			Logger logger = Logger.getLogger("io.firebus");
			logger.addHandler(new ConsoleHandler());
			logger.setLevel(lvl);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		new LoadTest();		
	}
}
