package com.nic.firebus.interfaces;

public interface DiscoveryListener
{
	public void nodeDiscovered(int id, String address, int port);
	
}
