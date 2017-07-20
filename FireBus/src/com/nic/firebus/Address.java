package com.nic.firebus;

import java.net.InetAddress;

public class Address
{
	protected InetAddress address;
	protected int port;
	protected Connection connection;
	
	public Address(InetAddress a, int p)
	{
		address = a;
		port = p;
	}
	
	public void setConnection(Connection c)
	{
		connection = c;
	}
	
	public InetAddress getInetAddress()
	{
		return address;
	}
	
	public int getPort()
	{
		return port;
	}

}
