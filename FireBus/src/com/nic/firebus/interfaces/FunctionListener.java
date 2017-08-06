package com.nic.firebus.interfaces;

import com.nic.firebus.Message;

public interface FunctionListener 
{
	public void functionCallback(Message inboundMessage, byte[] payload);
}
