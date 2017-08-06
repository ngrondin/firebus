package com.nic.firebus.interfaces;

import com.nic.firebus.Connection;
import com.nic.firebus.Message;

public interface ConnectionListener 
{
	public void connectionCreated(Connection c);
	
	public void messageReceived(Message m, Connection c);
	
	public void connectionClosed(Connection c);
}
