package com.nic.firebus.interfaces;


public interface Consumer extends BusFunction
{
	public void consume(byte[] payload);
}
