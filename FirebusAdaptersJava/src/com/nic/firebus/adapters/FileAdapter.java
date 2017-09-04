package com.nic.firebus.adapters;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

import com.nic.firebus.Node;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ConsumerInformation;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.interfaces.ServiceProvider;
import com.nic.firebus.utils.JSONObject;

public class FileAdapter extends FirebusAdapter implements ServiceProvider, Consumer
{
	protected String path;
	
	public FileAdapter(Node n, JSONObject c)
	{
		super(n, c);
		path = config.getString("path");
		String consumerName = config.getString("consumername");
		String serviceName = config.getString("servicename");
		if(consumerName != null)
			node.registerConsumer(new ConsumerInformation(consumerName), this, 10);
		if(serviceName != null)
			node.registerServiceProvider(new ServiceInformation(serviceName), this, 10);
	}

	public void consume(Payload payload)
	{
		// TODO Auto-generated method stub
		
	}

	public Payload service(Payload payload) throws FunctionErrorException
	{
		String fileName = new String(payload.data);
		try
		{
			HashMap<String, String> metadata = new HashMap<String, String>();
			File file = new File(path + "\\" + fileName);
			FileInputStream fis = new FileInputStream(file);
			metadata.put("filename", file.getName());
			byte[] bytes = new byte[fis.available()];
			fis.read(bytes);
			fis.close();
			Payload response = new Payload(metadata, bytes);
			return response;
		}
		catch(Exception e)
		{
			throw new FunctionErrorException(e.getMessage());
		}
	}

}
