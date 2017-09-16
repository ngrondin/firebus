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
import com.nic.firebus.Payload;
import com.nic.firebus.distributables.DistributableService;
import com.nic.firebus.information.ConsumerInformation;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.interfaces.BusFunction;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.interfaces.ServiceProvider;
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
		if(knownAddresses != null)
		{
			for(int i = 0; i < knownAddresses.size(); i++)
			{
				String address = knownAddresses.getObject(i).getString("address");
				int port = Integer.parseInt(knownAddresses.getObject(i).getString("port"));
				node.addKnownNodeAddress(address, port);
			}
		}
		
		JSONList adapters = config.getList("adapters");
		for(int i = 0; i < adapters.size(); i++)
		{
			try 
			{
				JSONObject adapter = adapters.getObject(i); 
				String type = adapter.getString("type");
				String serviceName = adapter.getString("servicename");
				String consumerName = adapter.getString("consumername");
				JSONObject adapterConfig = adapter.getObject("config");
				String className = adapterClasses.getProperty(type);
				if(className != null)
				{
					try
					{
						Class<?> c = Class.forName(className);
						Constructor<?> cons = c.getConstructor(new Class[]{Node.class, JSONObject.class});
						if(adapterConfig != null)
						{
							BusFunction func = (BusFunction)cons.newInstance(new Object[]{node, adapterConfig});
							if(serviceName != null  &&  func instanceof ServiceProvider)
								node.registerServiceProvider(new ServiceInformation(serviceName), ((ServiceProvider)func), 10);
							if(consumerName != null  &&  func instanceof Consumer)
								node.registerConsumer(new ConsumerInformation(consumerName), ((Consumer)func), 10);
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
				logger.severe(e.getMessage());
			}
		}
		
		JSONList services = config.getList("distributableservices");
		if(services != null)
		{
			for(int i = 0; i < services.size(); i++)
			{
				String serviceName = services.getString(i);
				Payload request = new Payload(serviceName.getBytes());
				try
				{
					Payload response = node.requestService("firebus_distributable_services_source", request);
					JSONObject serviceConfig = new JSONObject(response.getString());
					String type = serviceConfig.getString("type");
					DistributableService service = DistributableService.instantiate(node, type, serviceConfig.getObject("config"));
					node.registerServiceProvider(new ServiceInformation(serviceName), service, 10);
				}
				catch(Exception e)
				{
					logger.severe(e.getMessage());
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
