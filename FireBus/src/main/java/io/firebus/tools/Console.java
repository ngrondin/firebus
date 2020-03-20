package io.firebus.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.firebus.FirebusAdmin;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.information.NodeInformation;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.ServiceRequestor;
import io.firebus.logging.FirebusSimpleFormatter;

public class Console implements ServiceRequestor
{
	private Logger logger = Logger.getLogger("io.firebus");
	protected FirebusAdmin firebus;
	
	public Console(String[] args)
	{
		if(args.length == 0) {
			firebus = new FirebusAdmin();
		} else if(args.length == 1) {
			firebus = new FirebusAdmin(args[0], "firebuspassword0");
		} else if(args.length >- 2) {
			firebus = new FirebusAdmin(args[0], args[1]);
		}
	}
	public void run()
	{
		boolean quit = false;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while(!quit)
		{
			try
			{
				System.out.print("> ");
				String in = br.readLine();
				String[] parts = in.split(" ");
				String command = parts[0];
				
				if(command.equals("req") || command.equals("pub") || command.equals("si"))
				{
					String functionName = "";
					Payload payload = null;
					String inputFileName = null;
					if(parts.length > 1)
					{
						functionName = parts[1];
						if(parts.length > 2)
						{
							inputFileName = getSwitchValue(parts, "if");
							int dataStart = command.length() + functionName.length() + 2;
							for(int i = 2; i < parts.length; i+=2)
							{
								if(parts[i].startsWith("-"))
									dataStart += parts[i].length() + parts[i + 1].length() + 2;
								else
									break;
							}
							
							if(dataStart < in.length())
							{
								payload = new Payload(in.substring(dataStart).getBytes());
							}
							
							if(payload == null  &&  inputFileName != null)
							{
								try
								{
									FileInputStream fis = new FileInputStream(inputFileName);
									byte[] data = new byte[fis.available()];
									fis.read(data);
									fis.close();
									payload = new Payload(data);
									payload.metadata.put("filename", inputFileName);
								}
								catch(IOException e)
								{
									logger.severe(e.getMessage());
								}
							}
						}
					}
					
					if(payload == null)
					{
						payload = new Payload(new byte[0]);
					}

					if(command.equals("req") && functionName != null)
					{
						try
						{
							Payload response = firebus.requestService(functionName, payload, 2000);
							if(response.metadata.containsKey("filename"))
							{
								String fileName = response.metadata.get("filename");
								FileOutputStream fos = new FileOutputStream(fileName);
								fos.write(response.data);
								fos.close();
								System.out.println("Received file " + fileName);
							}
							else
							{
								System.out.println(response.getString());
							}
						}
						catch (FunctionErrorException e)
						{
							System.out.println("Function error: " + e.getMessage());
						}
						catch (FunctionTimeoutException e)
						{
							System.out.println("Request has timed out: " + e.getMessage());
						}
					}
					else if(command.equals("pub") && functionName != null)
					{
						firebus.publish(functionName, payload);
					}
					else if(command.equals("si")  &&  functionName != null)
					{
						ServiceInformation si = firebus.getServiceInformation(functionName);
						if(si != null)
							System.out.println(si);
					}
				}
				else if(command.equals("ni"))
				{
					int nodeId = Integer.parseInt(parts[1]);
					NodeInformation ni = firebus.getNodeInformation(nodeId);
					System.out.println(ni);
				}
				else if(command.equals("dir"))
				{
					NodeInformation[] nis = firebus.getNodeList();
					for(int i = 0; i < nis.length; i++)
						System.out.println(nis[i]);
				}
				else if(command.equals("exit"))
				{
					firebus.close();
					quit = true;
				}
			}
			catch(Exception e)
			{
				logger.severe(e.getMessage());
			}
			
		}
	}
	
	protected String getSwitchValue(String[] parts, String sw)
	{
		for(int i = 0; i < parts.length; i++)
		{
			if(parts[i].equals("-" + sw))
			{
				if(parts.length > i)
				{
					return parts[i + 1];
				}
			}
		}
		return null;
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
		c.run();
	}

}
