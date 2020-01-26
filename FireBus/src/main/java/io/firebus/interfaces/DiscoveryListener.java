package io.firebus.interfaces;

import io.firebus.Address;

public interface DiscoveryListener
{
	public void nodeDiscovered(int id, Address address);
	
}
