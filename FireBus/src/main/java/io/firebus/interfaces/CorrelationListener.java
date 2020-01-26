package io.firebus.interfaces;

import io.firebus.Message;

public interface CorrelationListener 
{
	public void correlatedResponseReceived(Message outMsg, Message inMsg);
	
	public void correlationTimedout(Message outMsg);	
}
