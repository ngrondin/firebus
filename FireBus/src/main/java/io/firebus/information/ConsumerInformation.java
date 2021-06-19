package io.firebus.information;


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

}
