package com.nic.firebus;

public interface ServiceProvider extends BusFunction
{

	public byte[] requestService(byte[] payload);
}
