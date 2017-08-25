package com.nic.firebus.interfaces;

import com.nic.firebus.Message;
import com.nic.firebus.exceptions.FunctionErrorException;

public interface FunctionListener 
{
	public void functionCallback(Message inboundMessage, byte[] payload);
	
	public void functionErrorCallback(Message inboundMessage, FunctionErrorException e);
}
