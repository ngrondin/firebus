package com.nic.firebus;

public interface Consumer extends BusFunction
{
	public void consume(byte[] payload);
}
