package com.nic.firebus;


public class Address
{
	protected String address;
	protected int port;
	protected Connection connection;
	
	public Address(String a, int p)
	{
		address = a;
		port = p;
	}
	
	public void setConnection(Connection c)
	{
		connection = c;
	}
	
	public String getAddress()
	{
		return address;
	}
	
	public int getPort()
	{
		return port;
	}
	
	public String toString()
	{
		return address + ":" + port;
	}

}
