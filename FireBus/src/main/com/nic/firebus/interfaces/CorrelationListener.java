package com.nic.firebus.interfaces;

import com.nic.firebus.Message;

public interface CorrelationListener 
{
	public void correlatedResponseReceived(Message outMsg, Message inMsg);
	
	public void correlationTimedout(Message outMsg);	
}
