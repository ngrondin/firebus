package io.firebus.interfaces;

import io.firebus.Connection;
import io.firebus.Message;

public interface ConnectionListener 
{
	public void connectionCreated(Connection c);
	
	public void connectionFailed(Connection c);
	
	public void messageReceived(Message m, Connection c);
	
	public void connectionClosed(Connection c);
	
}
