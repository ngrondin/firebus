package io.firebus;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class ConnectionServer extends Thread
{
	private Logger logger = Logger.getLogger("io.firebus");
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
					logger.fine("Starting connection server on " + port);
					server = new ServerSocket(port);
					successfulBind = true;
				}
				catch(Exception e)	
				{	
					logger.fine("Port " + port + " was already used");
					port++;
				}			
			}
		}
		else
		{
			port = p;
			logger.fine("Starting connection server on " + port);
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
					logger.severe(e.getMessage());
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
			logger.severe(e.getMessage());
		}
	}
}
