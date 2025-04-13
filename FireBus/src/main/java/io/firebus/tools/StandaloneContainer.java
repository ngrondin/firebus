package io.firebus.tools;

import java.io.FileInputStream;
import java.lang.reflect.Constructor;

import io.firebus.Firebus;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.interfaces.BusFunction;
import io.firebus.interfaces.Consumer;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.logging.Level;
import io.firebus.logging.Logger;

public class StandaloneContainer
{
	protected Firebus firebus;

	public StandaloneContainer(DataMap config)
	{
		if(config.containsKey("network") && config.containsKey("password")) {
			firebus = new Firebus(config.getString("network"), config.getString("password"));
		} else {
			firebus = new Firebus();
		}
		
		DataList knownAddresses = config.getList("knownaddresses");
		if(knownAddresses != null)
		{
			for(int i = 0; i < knownAddresses.size(); i++)
			{
				String address = knownAddresses.getObject(i).getString("address");
				int port = Integer.parseInt(knownAddresses.getObject(i).getString("port"));
				if(Logger.isLevel(Level.FINE)) Logger.fine("fb.container.addingknownaddress", new DataMap("address", address, "port", port));
				firebus.addKnownNodeAddress(address, port);
			}
		}
		
		DataList serviceConfigs = config.getList("services");
		if(serviceConfigs != null)
		{
			for(int i = 0; i < serviceConfigs.size(); i++)
			{
				try 
				{
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
							if(Logger.isLevel(Level.FINE)) Logger.fine("fb.container.addingservice", new DataMap("name", name));
							BusFunction service = (BusFunction)cons.newInstance(new Object[]{firebus, deploymentConfig});
							if(service instanceof ServiceProvider)
								firebus.registerServiceProvider(name, ((ServiceProvider)service), 10);
							if(service instanceof Consumer)
								firebus.registerConsumer(name, ((Consumer)service), 10);
						}
						catch(Exception e)
						{
							Logger.severe("fb.container.classnotfound", new DataMap("class", className));
						}
					}
					else
					{
						Logger.severe("fb.container.noclassgiven");
					}
				}
				catch(Exception e)
				{
					Logger.severe("fb.container.error", e);
				}
			}
		}
	}
	
	public static void main(String[] args)
	{
		if(args.length > 0)
		{			
			try
			{
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
