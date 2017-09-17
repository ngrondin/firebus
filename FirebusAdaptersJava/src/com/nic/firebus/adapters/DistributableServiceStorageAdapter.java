package com.nic.firebus.adapters;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.interfaces.ServiceProvider;
import com.nic.firebus.utils.JSONObject;

public class DistributableServiceStorageAdapter extends Adapter implements ServiceProvider
{
	private Logger logger = Logger.getLogger("com.nic.firebus.adapters");
	protected HashMap<String, JSONObject> serviceConfigs;
	protected String path;


	public DistributableServiceStorageAdapter(Firebus n, JSONObject c)
	{
		super(n, c);
		serviceConfigs = new HashMap<String, JSONObject>();
		path = config.getString("path");
		if(path == null)
			path = ".";
		if(!path.endsWith(File.separator))
			path = path + File.separator;
		
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
						JSONObject functionConfig = new JSONObject(new FileInputStream(list[i]));
						String serviceName = functionConfig.getString("servicename");
						if(serviceName != null)
						{
							serviceConfigs.put(serviceName, functionConfig);
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


	public Payload service(Payload payload) throws FunctionErrorException
	{
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

}
