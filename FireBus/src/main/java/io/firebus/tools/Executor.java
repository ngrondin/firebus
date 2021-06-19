package io.firebus.tools;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Logger;

import io.firebus.FirebusAdmin;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.information.FunctionInformation;
import io.firebus.information.NodeInformation;

public class Executor {
	private Logger logger = Logger.getLogger("io.firebus");
	protected FirebusAdmin firebus;

	public Executor(String[] args) {
		for(int i = 0; i < args.length; i++) {
			String swt = args[i];
			if(swt.equals("-fb") && args.length > i + 1) {
				String param = args[++i];
				String[] parts = param.split("/");
				firebus = new FirebusAdmin(parts[0], parts[1]);
			} else if(swt.equals("-ka") && args.length > i + 1) {
				String param = args[++i];
				String[] parts = param.split(":");
				firebus.addKnownNodeAddress(parts[0], Integer.parseInt(parts[1]));
			}
		}
		
		
		if(firebus == null) 
		{
			firebus = new FirebusAdmin();
		}		
	}
	
	
	public String execute(String line, Properties headers) throws IOException 
	{
		String ret = "";
		String[] parts = line.split(" ");
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
					
					if(dataStart < line.length())
					{
						payload = new Payload(line.substring(dataStart).getBytes());
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
				payload = new Payload();
			}
			
			Iterator<Object> it = headers.keySet().iterator();
			while(it.hasNext())
			{
				String key = (String)it.next();
				payload.metadata.put(key, headers.getProperty(key));
			}

			if(command.equals("req") && functionName != null)
			{
				try
				{
					Payload response = firebus.requestService(functionName, payload, 10000);
					if(response.metadata.containsKey("filename"))
					{
						String fileName = response.metadata.get("filename");
						FileOutputStream fos = new FileOutputStream(fileName);
						fos.write(response.data);
						fos.close();
						ret = "Received file " + fileName;
					}
					else
					{
						System.out.println(response.getString());
					}
				}
				catch (FunctionErrorException e)
				{
					ret = "Function error: " + e.getMessage();
				}
				catch (FunctionTimeoutException e)
				{
					ret = "Request has timed out: " + e.getMessage();
				}
			}
			else if(command.equals("pub") && functionName != null)
			{
				firebus.publish(functionName, payload);
			}
		}
		else if(command.equals("ni"))
		{
			int nodeId = Integer.parseInt(parts[1]);
			NodeInformation ni = firebus.getNodeInformation(nodeId);
			if(ni != null)
				ret = ni.toString();
		}
		else if(command.equals("dir"))
		{
			NodeInformation[] nis = firebus.getNodeList();
			for(int i = 0; i < nis.length; i++)
				ret = ret + nis[i] + "\r\n";
		}
		else if(command.equals("aa"))
		{
			String address = parts[1];
			int port = Integer.valueOf(parts[2]);
			firebus.addKnownNodeAddress(address, port);
		}
		return ret;
	}
	
	public void close() {
		try {
			firebus.close();
		} catch(Exception e) {
			e.printStackTrace();
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
}
