package io.firebus;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import io.firebus.data.DataMap;
import io.firebus.logging.Logger;

public class ConnectionServer extends Thread
{
	protected ConnectionManager connectionManager;
	protected boolean quit;
	protected ServerSocket server;
	protected int port;

	
	public ConnectionServer(ConnectionManager cm, int p)  throws IOException 
	{
		connectionManager = cm;
		quit = false;
		if(p == 0)
		{
			port = 1990;
			boolean successfulBind = false;
			while(!successfulBind)
			{
				try
				{
					Logger.fine("fb.connections.starting", new DataMap("port", port));
					server = new ServerSocket(port);
					successfulBind = true;
				}
				catch(Exception e)	
				{	
					Logger.fine("fb.connections.alreadyused", new DataMap("port", port));
					port++;
				}			
			}
		}
		else
		{
			port = p;
			Logger.fine("fb.connections.starting", new DataMap("port", port));
			server = new ServerSocket(port);
		}		
		setName("fbConnServer");
		start();
	}

	public void run()
	{
		while(!quit)
		{
			try 
			{
				while(!quit)
				{
					Socket socket = server.accept();
					connectionManager.socketReceived(socket, port);
				}
			} 
			catch (Exception e) 
			{
				if(!quit)
					Logger.severe("fb.connections.receiving", e);
				
			}
		}		
	}
	
	public int getPort()
	{
		return port;
	}
	
	public void close()
	{
		try
		{
			quit = true;
			server.close();
		}
		catch(Exception e)
		{
			Logger.severe("fb.connections.closing", e);
		}
	}
}
