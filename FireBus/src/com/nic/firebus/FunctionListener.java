package com.nic.firebus;

public interface FunctionListener 
{
	public void functionCallback(Message inboundMessage, byte[] payload);
}
