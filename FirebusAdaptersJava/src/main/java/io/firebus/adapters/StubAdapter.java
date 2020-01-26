package io.firebus.adapters;

import java.util.Random;

import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.utils.DataMap;

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
