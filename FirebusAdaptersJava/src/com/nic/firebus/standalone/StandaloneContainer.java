package com.nic.firebus.standalone;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.nic.firebus.Node;
import com.nic.firebus.logging.FirebusSimpleFormatter;
import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;

public class StandaloneContainer
{
	private Logger logger = Logger.getLogger("com.nic.firebus.standalone");
	protected Node node;

	public StandaloneContainer(JSONObject config)
	{
		Properties adapterClasses = new Properties();
		try
		{
			InputStream is = getClass().getResourceAsStream("/com/nic/firebus/adapters/AdapterClasses.properties");
			adapterClasses.load(is);
		}
		catch(IOException e)
		{
			logger.severe(e.getMessage());
		}
		
		node = new Node(config.getString("network"), config.getString("password"));
		JSONList knownAddresses = config.getList("knownaddresses");
		for(int i = 0; i < knownAddresses.size(); i++)
		{
			String address = knownAddresses.getObject(i).getString("address");
			int port = Integer.parseInt(knownAddresses.getObject(i).getString("port"));
			node.addKnownNodeAddress(address, port);
		}
		
		JSONList adapters = config.getList("adapters");
		for(int i = 0; i < adapters.size(); i++)
		{
			try 
			{
				String type = adapters.getObject(i).getString("type");
				JSONObject adapterConfig = adapters.getObject(i).getObject("config");
				String className = adapterClasses.getProperty(type);
				if(className != null  &&  adapterConfig != null)
				{
					Class<?> c = Class.forName(className);
					Constructor<?> cons = c.getConstructor(new Class[]{Node.class, JSONObject.class});
					cons.newInstance(new Object[]{node, adapterConfig});
				}
				else
				{
					logger.severe("Adapter of type " + type + " has no defined class or has no configuration");
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args)
	{

		if(args.length > 0)
		{			
			try
			{
				Logger.getLogger("").removeHandler(Logger.getLogger("").getHandlers()[0]);
				Logger logger = Logger.getLogger("com.nic.firebus");
				FileHandler fh = new FileHandler("FirebusStandaloneContainer.log");
				fh.setFormatter(new FirebusSimpleFormatter());
				fh.setLevel(Level.FINEST);
				logger.addHandler(fh);
				logger.setLevel(Level.FINEST);

				JSONObject config = new JSONObject(new FileInputStream(args[0]));
				new StandaloneContainer(config);
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
}
