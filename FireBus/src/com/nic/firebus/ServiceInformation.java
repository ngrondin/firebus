package com.nic.firebus;

public class ServiceInformation 
{
	protected String serviceName;
	
	public ServiceInformation(String sn)
	{
		serviceName = sn;
	}

	public String getServiceName()
	{
		return serviceName;
	}
	
	public String toString()
	{
		return serviceName;
	}
}
