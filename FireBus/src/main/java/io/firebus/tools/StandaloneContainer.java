package io.firebus.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.interfaces.BusFunction;
import io.firebus.interfaces.Consumer;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.logging.FirebusSimpleFormatter;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;

public class StandaloneContainer
{
	private Logger logger = Logger.getLogger("com.nic.firebus.standalone");
	protected Firebus firebus;

	public StandaloneContainer(DataMap config)
	{
		firebus = new Firebus(config.getString("network"), config.getString("password"));
		DataList knownAddresses = config.getList("knownaddresses");
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
		
		List<Logger> loggers = new ArrayList<Logger>();
		DataList loggerConfigs = config.getList("loggers");
		for(int i = 0; i < loggerConfigs.size(); i++)
		{
			try
			{
				DataMap loggerConfig = loggerConfigs.getObject(i);
				Logger logger = Logger.getLogger(loggerConfig.getString("name"));
				Formatter formatter = (Formatter)Class.forName(loggerConfig.getString("formatter")).newInstance();
				FileHandler fileHandler = new FileHandler(loggerConfig.getString("filename"));
				fileHandler.setFormatter(formatter);
				fileHandler.setLevel(Level.parse(loggerConfig.getString("level")));
				logger.addHandler(fileHandler);
				logger.setUseParentHandlers(false);
				logger.setLevel(Level.parse(loggerConfig.getString("level")));
				loggers.add(logger);
			}
			catch(Exception e)
			{
				logger.severe("General error when configuring loggers : " + e.getMessage());
			}
		}
		
		DataList serviceConfigs = config.getList("services");
		for(int i = 0; i < serviceConfigs.size(); i++)
		{
			try 
			{
				logger.fine("Adding services to container");
				DataMap serviceConfig = serviceConfigs.getObject(i); 
				String className = serviceConfig.getString("class");
				String name = serviceConfig.getString("name");
				DataMap deploymentConfig = serviceConfig.getObject("config");
				if(className != null && name != null)
				{
					try
					{
						Class<?> c = Class.forName(className);
						Constructor<?> cons = c.getConstructor(new Class[]{Firebus.class, DataMap.class});
						logger.fine("Instantiating service " + name);
						BusFunction service = (BusFunction)cons.newInstance(new Object[]{firebus, deploymentConfig});
						if(service instanceof ServiceProvider)
							firebus.registerServiceProvider(name, ((ServiceProvider)service), 10);
						if(service instanceof Consumer)
							firebus.registerConsumer(name, ((Consumer)service), 10);
					}
					catch(Exception e)
					{
						logger.severe("Class " + className + " cannot be found in the classpath");
					}
				}
				else
				{
					logger.severe("No class or name provided for service");
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
				fh.setLevel(Level.FINEST);
				logger.addHandler(fh);
				logger.setLevel(Level.FINEST);

				DataMap config = new DataMap(new FileInputStream(args[0]));
				new StandaloneContainer(config);
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
}
