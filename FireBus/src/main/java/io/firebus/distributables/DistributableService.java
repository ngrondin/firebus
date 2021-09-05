package io.firebus.distributables;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Properties;
import java.util.logging.Logger;

import io.firebus.NodeCore;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.data.DataMap;

public abstract class DistributableService implements ServiceProvider
{
	protected static Properties serviceClasses;

	protected NodeCore nodeCore;
	protected DataMap config;
	protected long expiry;
	
	public DistributableService(NodeCore nc, DataMap c)
	{
		nodeCore = nc;
		config = c;
		expiry = System.currentTimeMillis() + 315360000000L;
		if(config.get("expires") != null)
			setValidityTime(Integer.parseInt(config.getString("expires")));
	}
	
	public void setValidityTime(int v)
	{
		expiry = System.currentTimeMillis() + (1000 * v);
	}
	
	public boolean isExpired()
	{
		return System.currentTimeMillis() > expiry;
	}

	public static DistributableService instantiate(NodeCore nodeCore, String type, DataMap config)
	{
		Logger logger = Logger.getLogger("io.firebus");
		DistributableService service = null;
		
		if(serviceClasses == null)
		{
			serviceClasses = new Properties();;
			try
			{
				InputStream is = DistributableService.class.getResourceAsStream("/com/nic/firebus/distributables/serviceClasses.properties");
				serviceClasses.load(is);
			}
			catch(IOException e)
			{
				logger.severe(e.getMessage());
			}

		}
		
		String className = serviceClasses.getProperty(type);
		if(className != null)
		{
			try
			{
				Class<?> c = Class.forName(className);
				Constructor<?> cons = c.getConstructor(new Class[]{NodeCore.class, DataMap.class});
				if(config != null)
				{
					service = (DistributableService)cons.newInstance(new Object[]{nodeCore, config});
				}
				else
				{
					logger.severe("No configuration has been defined for service " + type);
				}
			}
			catch(Exception e)
			{
				logger.severe("Class " + className + " cannot be found in the classpath");
			}
		}
		else
		{
			logger.severe("Service of type " + type + " does not have a defined class");
		}

		return service;
	}
}
