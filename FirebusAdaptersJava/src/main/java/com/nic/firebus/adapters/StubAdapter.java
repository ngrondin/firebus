package com.nic.firebus.adapters;

import java.util.Random;

import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.interfaces.ServiceProvider;
import com.nic.firebus.utils.DataMap;

public class StubAdapter extends Adapter implements ServiceProvider
{

	public StubAdapter(DataMap c)
	{
		super(c);
	}

	public Payload service(Payload payload) throws FunctionErrorException
	{
		Payload response = new Payload();
		try
		{
			DataMap request = new DataMap(payload.getString());
			if(request.getString("type").equals("random"))
			{
				int size = request.getNumber("size").intValue();
				long delay = request.getNumber("delay").longValue();
				Thread.sleep(delay);
				byte[] bytes = new byte[size];
				Random rnd = new Random();
				for(int i = 0; i < size; i++)
					bytes[i] = (byte)rnd.nextInt();
				response.setData(bytes);
			}
		}
		catch(Exception e)
		{
			throw new FunctionErrorException("StubAdapter exception", e);
		}

		return response;
	}

	public ServiceInformation getServiceInformation()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
