package com.nic.firebus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Connection extends Thread 
{
	protected Socket socket;
	protected InputStream is;
	protected OutputStream os;
	protected ConnectionListener listener;
	protected boolean quit;
	protected int msgState;
	protected int msgLen;
	protected int msgPos;
	protected int msgCRC;
	protected byte[] msg;
	
	public Connection(Socket s, ConnectionListener l) throws IOException
	{
		socket = s;
		is = s.getInputStream();
		os = s.getOutputStream();
		listener = l;
		quit = false;
		msgState = 0;
		start();
	}
	
	
	public String getRemoteAddress()
	{
		return socket.getInetAddress().getHostAddress();
	}
	/*
	public String getLocalAddress()
	{
		return socket.getLocalAddress().getHostAddress();
	}
	*/
	public void run()
	{
		while(!quit)
		{
			try 
			{
				int i = is.read();
				if(msgState == 0)
				{
					if(i == 0x7E)
						msgState = 1;
				}
				else if(msgState == 1)
				{
					msgLen = i;
					msgState = 2;
				}
				else if(msgState == 2)
				{
					msgLen += (256 * i);
					msgState = 3;
					msgPos = 0;
					msgCRC = 0;
					msg = new byte[msgLen];
				}
				else if(msgState == 3)
				{
					msg[msgPos] = (byte)i;
					msgCRC = (msgCRC ^ i) & 0x00FF;
					msgPos++;
					if(msgPos == msgLen)
						msgState = 4;
				}
				else if(msgState == 4)
				{
					if(i == msgCRC)
					{
						Message message = new Message(msg, this);
						if(listener != null)
							listener.messageReceived(message, this);
					}
					msgState = 0;
				}
			} 
			catch (IOException e) 
			{
				close();
				listener.connectionClosed(this);
			}
		}
	}
	
	public void sendMessage(Message msg)
	{
		try
		{
			os.write(msg.getEncodedMessage());
			os.flush();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void close()
	{
		quit = true;
		try 
		{
			socket.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}
