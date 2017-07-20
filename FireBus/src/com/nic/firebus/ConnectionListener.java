package com.nic.firebus;

public interface ConnectionListener 
{
	public void messageReceived(Message m, Connection c);
	
	public void connectionClosed(Connection c);
}
