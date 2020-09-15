package io.firebus.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.interfaces.ServiceRequestor;
import io.firebus.logging.FirebusSimpleFormatter;

public class Console implements ServiceRequestor
{
	private Logger logger = Logger.getLogger("io.firebus");
	protected Executor executor;
	protected String command;
	protected boolean quit;
	protected Properties headers;
	
	public Console(String[] args)
	{
		quit = false;
		executor = new Executor(args);

		headers = new Properties();
		File headerFile = new File("ConsoleHeaders.properties");
		if(headerFile.exists()) 
		{
			try 
			{
				headers.load(new FileInputStream(headerFile));
			}
			catch(Exception e)
			{ 
				System.out.println("Error loading headers : " + e.getMessage());
			}
		}
	}
	
	public void run()
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while(!quit)
		{
			try
			{
				System.out.print("> ");
				String in = br.readLine();
				if(in.equals("quit")) {
					quit = true;
				} else {
					System.out.println(executor.execute(in, headers));
				}
			}
			catch(Exception e)
			{
				logger.severe(e.getMessage());
			}
		}
		executor.close();			
	}
	

	
	public void requestCallback(Payload payload)
	{
		System.out.println(new String(payload.data));
		
	}

	public void requestErrorCallback(FunctionErrorException e)
	{
		System.out.println("Error: " + e.getMessage());		
	}

	public void requestTimeout()
	{
		System.out.println("Timed out");
	}


	public static void main(String[] args)
	{
		try
		{
			Logger.getLogger("").removeHandler(Logger.getLogger("").getHandlers()[0]);
			Logger logger = Logger.getLogger("io.firebus");
			FileHandler fh = new FileHandler("Console.log");
			fh.setFormatter(new FirebusSimpleFormatter());
			fh.setLevel(Level.FINER);
			logger.addHandler(fh);
			logger.setLevel(Level.FINER);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		Console c = new Console(args);
		//try { Thread.sleep(5000); } catch(Exception e) {}
		c.run();
	}

}
