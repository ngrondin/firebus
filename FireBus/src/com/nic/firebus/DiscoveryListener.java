package com.nic.firebus;

public interface DiscoveryListener
{
	public void nodeDiscovered(int id, String address, int port);
	
}
