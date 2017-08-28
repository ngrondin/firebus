package com.nic.firebus.interfaces;

import com.nic.firebus.Payload;


public interface Consumer extends BusFunction
{
	public void consume(Payload payload);
}
