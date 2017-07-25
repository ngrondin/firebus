package com.nic.firebus;

public interface FunctionListener 
{
	public void functionCallback(int correlation, byte[] payload);
}
