package com.nic.firebus.interfaces;


public interface ServiceProvider extends BusFunction
{

	public byte[] requestService(byte[] payload);
}
