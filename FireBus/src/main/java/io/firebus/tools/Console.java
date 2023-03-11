package io.firebus.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;

import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.interfaces.ServiceRequestor;
import io.firebus.logging.FirebusSimpleFormatter;

public class Console implements ServiceRequestor
{
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
				e.printStackTrace();
			}
		}
		executor.close();			
	}
	

	
	public void response(Payload payload)
	{
		System.out.println(payload.getString());
		
	}

	public void error(FunctionErrorException e)
	{
		System.out.println("Error: " + e.getMessage());		
	}

	public void timeout()
	{
		System.out.println("Timed out");
	}


	public static void main(String[] args)
	{
		Console c = new Console(args);
		c.run();
	}

}
