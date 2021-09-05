package io.firebus.information;

import io.firebus.data.DataMap;

public class ConsumerInformation extends FunctionInformation
{
	public ConsumerInformation(String sn)
	{
		super(sn);
	}

	public ConsumerInformation(NodeInformation ni, String sn)
	{
		super(ni, sn);
	}

	public byte[] serialise() 
	{
		return null;
	}

	public void deserialise(byte[] bytes) 
	{
		
	}

	public String toString() 
	{
		return "Consumer : " + name;
	}
	
	public DataMap getStatus()
	{
		DataMap status = new DataMap();
		status.put("type", "consumer");
		return status;
	}

}
