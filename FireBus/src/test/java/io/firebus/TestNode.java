package io.firebus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.Consumer;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.interfaces.ServiceRequestor;
import io.firebus.logging.FirebusSimpleFormatter;


public class TestNode
{
	public static void main(String[] args)
	{
		Logger.getLogger("").removeHandler(Logger.getLogger("").getHandlers()[0]);
		try
		{
			FileHandler fh = new FileHandler("TestNode.log");
			fh.setFormatter(new FirebusSimpleFormatter());
			fh.setLevel(Level.FINEST);
			Logger logger = Logger.getLogger("io.firebus");
			logger.addHandler(fh);
			logger.setLevel(Level.FINEST);

			Firebus n = new Firebus();
			
			if(args.length > 0)
			{
				if(args[0].equals("console"))
				{
					if(args.length > 1)
					{
						String functionName = args[1];
						boolean quit = false;
						BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
						while(!quit)
						{
							try
							{
								String in = br.readLine();
								String[] parts = in.split(" ");
								if(parts[0].equals("info"))
								{
									/*FunctionInformation si = n.getFunctionInformation(functionName);
									if(si != null)
										System.out.println(si.toString());*/
								}
								else if(parts[0].equals("req"))
								{
									String line = in.substring(parts[0].length() + 1);
									n.requestService(functionName, new Payload(null, line.getBytes()), new ServiceRequestor() {
										public void response(Payload payload) {
											System.out.println(payload.getString());
										}
										public void timeout() {
											System.out.println("Timed out");
										}
										public void error(FunctionErrorException e) {
											System.out.println("Error: " + e.getMessage());
										}}, "testrequestor", 10000);
								}
								else if(parts[0].equals("reqs"))
								{
									String line = in.substring(parts[0].length() + 1);
									try
									{
										String resp = n.requestService(functionName, new Payload(line.getBytes()), 2000).getString();
										System.out.println(resp);
									}
									catch (Exception e)
									{
										e.printStackTrace();
									}
								}
								else if(parts[0].equals("pub"))
								{
									String line = in.substring(parts[0].length());
									n.publish(functionName, new Payload(line.getBytes()));
								}
							} 
							catch (IOException e) {}
						}
					}
				}
				
				if(args[0].equals("provider"))
				{
					if(args.length > 2)
					{
						final String prefix = args[2];
						n.registerServiceProvider(args[1], new ServiceProvider() {
							public Payload service(Payload payload) throws FunctionErrorException
							{
								System.out.println("Providing Service");
								//try{ Thread.sleep(3000); } catch(Exception e) {}
								String val = payload.getString();
								if(val.equals("throw"))
									throw new FunctionErrorException("this is my error");
								else
									return new Payload(prefix + " " + payload.getString());
							}

							public ServiceInformation getServiceInformation()
							{
								return  null;//new ServiceInformation("text/plain", "{request:String}", "text/plain", "{response:String}");
							}
						}, 2);
						System.out.println("Service Provider Registered");
					}
				}

				
				if(args[0].equals("consumer"))
				{
					if(args.length > 1)
					{
						n.registerConsumer(args[1], new Consumer(){
							public void consume(Payload payload)
							{
								System.out.println(payload.getString());
							}
						}, 10);
						System.out.println("Consumer Registered");
					}
				}
			}		
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}


	}
}
