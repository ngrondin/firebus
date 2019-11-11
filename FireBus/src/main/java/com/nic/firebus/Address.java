package com.nic.firebus;


public class Address
{
	protected String address;
	protected int port;
	
	public Address(String a, int p)
	{
		address = a;
		port = p;
	}
	

	public String getIPAddress()
	{
		return address;
	}
	
	public int getPort()
	{
		return port;
	}
	
	public boolean equals(Address a)
	{
		if(a != null && a.getIPAddress() != null && a.getIPAddress().equals(address)  &&  a.getPort() == port)
			return true;
		else
			return false;
	}
	
	public String toString()
	{
		return address + ":" + port;
	}

}
