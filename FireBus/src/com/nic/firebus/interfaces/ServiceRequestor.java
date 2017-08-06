package com.nic.firebus.interfaces;

public interface ServiceRequestor 
{
	public void requestCallback(byte[] payload);
	
	public void requestTimeout();
}
