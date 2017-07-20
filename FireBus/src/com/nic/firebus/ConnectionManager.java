package com.nic.firebus;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ConnectionManager extends Thread
{
	protected int port;
	protected boolean quit;
	protected ServerSocket server;
	protected Node node;
	protected ArrayList<Connection> connections;
	
	public ConnectionManager(int p, Node n)
	{
		connections = new ArrayList<Connection>();
		port = p;
		quit = false;
		node = n;
		start();
	}

	public void run()
	{
		while(!quit)
		{
			try 
			{
				server = new ServerSocket(port);
				Socket socket = server.accept();
				Connection connection = new Connection(socket, node);
				connections.add(connection);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	public Connection createConnection(InetAddress a, int p) throws IOException
	{
		Socket socket = new Socket(a, p);
		Connection c = new Connection(socket, node);
		connections.add(c);
		return c;
	}
	
	public void dropConnection(Connection c)
	{
		c.close();
		connections.remove(c);
	}

	public InetAddress getAddress()
	{
		return server.getInetAddress();
	}
	
	public int getPort()
	{
		return server.getLocalPort();
	}
}
