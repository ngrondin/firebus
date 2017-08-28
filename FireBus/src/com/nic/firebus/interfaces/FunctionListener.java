package com.nic.firebus.interfaces;

import com.nic.firebus.Message;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;

public interface FunctionListener 
{
	public void functionCallback(Message inboundMessage, Payload payload);
	
	public void functionErrorCallback(Message inboundMessage, FunctionErrorException e);
}
