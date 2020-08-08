package io.firebus.information;


public class ConsumerInformation extends FunctionInformation
{
	protected String consumerName;
	
	public ConsumerInformation(String sn)
	{
		consumerName = sn;
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
		return "Consumer : " + consumerName;
	}

}
