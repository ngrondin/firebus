package com.nic.firebus.adapters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.logging.Logger;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.interfaces.ServiceProvider;
import com.nic.firebus.utils.DataMap;

public class FileAdapter extends Adapter implements ServiceProvider, Consumer
{
	private Logger logger = Logger.getLogger("com.nic.firebus.adapters");
	protected String path;
	
	public FileAdapter(Firebus n, DataMap c)
	{
		super(n, c);
		path = config.getString("path");
	}

	public void consume(Payload payload)
	{
		try
		{
			String fileName = payload.metadata.get("filename");
			if(fileName == null)
				fileName = "firebusfile.txt";
			File file = new File(path + File.separator + fileName);
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(payload.data);
			fos.close();
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
		}
	}

	public Payload service(Payload payload) throws FunctionErrorException
	{
		String fileName = new String(payload.data);
		try
		{
			HashMap<String, String> metadata = new HashMap<String, String>();
			File file = new File(path + File.separator + fileName);
			byte[] bytes = null;
			if(!file.isDirectory())
			{
				FileInputStream fis = new FileInputStream(file);
				metadata.put("filename", file.getName());
				bytes = new byte[fis.available()];
				fis.read(bytes);
				fis.close();
			}
			else
			{
				StringBuilder sb = new StringBuilder();
				String[] list = file.list();
				for(int i = 0; i < list.length; i++)
				{
					if(i > 0)
						sb.append("\r\n");
					sb.append(list[i]);
				}
				bytes = sb.toString().getBytes();
			}
			Payload response = new Payload(metadata, bytes);
			return response;
		}
		catch(Exception e)
		{
			throw new FunctionErrorException(e.getMessage());
		}
	}

	public ServiceInformation getServiceInformation()
	{
		return new ServiceInformation("text/plain", "", "text/plain", "");
	}

}
