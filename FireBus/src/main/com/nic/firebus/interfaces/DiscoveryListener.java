package com.nic.firebus.interfaces;

import com.nic.firebus.Address;

public interface DiscoveryListener
{
	public void nodeDiscovered(int id, Address address);
	
}
