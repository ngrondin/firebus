package com.nic.firebus.standalone;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.nic.firebus.Firebus;
import com.nic.firebus.adapters.Adapter;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.interfaces.ServiceProvider;
import com.nic.firebus.logging.FirebusSimpleFormatter;
import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;

public class StandaloneContainer
{
	private Logger logger = Logger.getLogger("com.nic.firebus.standalone");
	protected Firebus firebus;

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
		
		firebus = new Firebus(config.getString("network"), config.getString("password"));
		JSONList knownAddresses = config.getList("knownaddresses");
		if(knownAddresses != null)
		{
			for(int i = 0; i < knownAddresses.size(); i++)
			{
				String address = knownAddresses.getObject(i).getString("address");
				int port = Integer.parseInt(knownAddresses.getObject(i).getString("port"));
				logger.fine("Adding known address " + address + ":" + port);
				firebus.addKnownNodeAddress(address, port);
			}
		}
		
		JSONList adapters = config.getList("adapters");
		for(int i = 0; i < adapters.size(); i++)
		{
			try 
			{
				logger.fine("Adding adapter to container");
				JSONObject depploymentConfig = adapters.getObject(i); 
				String type = depploymentConfig.getString("type");
				String serviceName = depploymentConfig.getString("servicename");
				String consumerName = depploymentConfig.getString("consumername");
				JSONObject adapterConfig = depploymentConfig.getObject("config");
				String className = adapterClasses.getProperty(type);
				if(className != null)
				{
					try
					{
						Class<?> c = Class.forName(className);
						Constructor<?> cons = c.getConstructor(new Class[]{Firebus.class, JSONObject.class});
						if(adapterConfig != null)
						{
							logger.fine("Instantiating new adapter of type " + type);
							Adapter adapter = (Adapter)cons.newInstance(new Object[]{firebus, adapterConfig});
							if(serviceName != null  &&  adapter instanceof ServiceProvider)
								firebus.registerServiceProvider(serviceName, ((ServiceProvider)adapter), 10);
							if(consumerName != null  &&  adapter instanceof Consumer)
								firebus.registerConsumer(consumerName, ((Consumer)adapter), 10);
						}
						else
						{
							logger.severe("No configuration has been defined for adapter " + type);
						}
					}
					catch(Exception e)
					{
						logger.severe("Class " + className + " cannot be found in the classpath");
					}
				}
				else
				{
					logger.severe("Adapter of type " + type + " does not have a defined class");
				}
			}
			catch(Exception e)
			{
				logger.severe("General error message when instantiating a new adapter: " + e.getMessage());
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
				fh.setLevel(Level.INFO);
				logger.addHandler(fh);
				logger.setLevel(Level.INFO);

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
