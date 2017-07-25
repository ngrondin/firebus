package com.nic.firebus;

public interface ConnectionListener 
{
	public void connectionCreated(Connection c);
	
	public void messageReceived(Message m, Connection c);
	
	public void connectionClosed(Connection c);
}
