package io.firebus.adapters;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.utils.DataException;
import io.firebus.utils.DataMap;

public class DistributableServiceStorageAdapter extends Adapter implements ServiceProvider
{
	private Logger logger = Logger.getLogger("io.firebus.adapters");
	protected HashMap<String, DataMap> serviceConfigs;
	protected String path;


	public DistributableServiceStorageAdapter(DataMap c)
	{
		super(c);
		serviceConfigs = new HashMap<String, DataMap>();
		path = config.getString("path");
		if(path == null)
			path = ".";
		if(!path.endsWith(File.separator))
			path = path + File.separator;
		

	}


	public Payload service(Payload payload) throws FunctionErrorException
	{
		refreshConfigs();
		Payload response = new Payload();;
		String serviceName = payload.getString();
		if(serviceName.equals(""))
		{
			StringBuilder sb = new StringBuilder();
			Iterator<String> it = serviceConfigs.keySet().iterator();
			while(it.hasNext())
			{
				if(sb.length() > 0)
					sb.append("\r\n");
				sb.append(it.next());				
			}
			response.setData(sb.toString());
		}
		else if(serviceConfigs.containsKey(serviceName))
		{
			response.setData(serviceConfigs.get(serviceName).toString());
		}
		return response;
	}

	
	public ServiceInformation getServiceInformation()
	{
		return null;//new ServiceInformation("text/plain", "", "text/plain", "");
	}

	protected void refreshConfigs()
	{
		try
		{
			File dir = new File(path);
			if(dir.isDirectory())
			{
				File[] list = dir.listFiles();
				for(int i = 0; i < list.length; i++)
				{
					File file = list[i];
					if(file.isFile()  &&  file.getName().toUpperCase().endsWith("JSON"))
					{
						try
						{
							DataMap functionConfig = new DataMap(new FileInputStream(list[i]));
							String serviceName = functionConfig.getString("servicename");
							if(serviceName != null)
							{
								serviceConfigs.put(serviceName, functionConfig);
							}
						}
						catch(DataException e)
						{
							logger.info("Error reading config file : " + file.getName() + " (" + e.getMessage() + ")");
						}
					}							
				}
			}
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
		}
	}
	
}
