package io.firebus.interfaces;

import io.firebus.Message;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;

public interface FunctionListener 
{
	public void functionCallback(Message inboundMessage, Payload payload);
	
	public void functionErrorCallback(Message inboundMessage, FunctionErrorException e);
}
