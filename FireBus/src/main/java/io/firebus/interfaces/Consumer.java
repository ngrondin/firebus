package io.firebus.interfaces;

import io.firebus.Payload;


public interface Consumer extends BusFunction
{
	public void consume(Payload payload);
}
