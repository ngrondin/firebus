package com.nic.firebus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

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
	//protected Address address;
	protected NodeInformation nodeInformation;
	
	public Connection(Socket s, ConnectionListener l) throws IOException
	{
		socket = s;
		is = s.getInputStream();
		os = s.getOutputStream();
		listener = l;
		quit = false;
		msgState = 0;
		setName("Firebus Connection");
		start();
	}
	
	public Connection(Address a, ConnectionListener l) throws UnknownHostException, IOException
	{
		socket = new Socket(a.getAddress(), a.getPort());
		is = socket.getInputStream();
		os = socket.getOutputStream();
		//setAddress(a);
		listener = l;
		quit = false;
		msgState = 0;
		setName("Firebus Connection");
		start();
	}
	
	/*
	public void setAddress(Address a)
	{
		if((address != null && a == null) || (address == null && a != null) || (address != null && a != null && a != address))
		{
			Address oldAddress = address;
			address = a;
			if(oldAddress != null)
				oldAddress.setConnection(null);
			if(address != null)
				address.setConnection(this);
		}
	}
	
	public void setNodeInformation(NodeInformation ni)
	{
		if((nodeInformation != null && ni == null) || (nodeInformation == null && ni != null) || (nodeInformation != null && ni != null && ni != nodeInformation))
		{
			NodeInformation oldNI = nodeInformation;
			nodeInformation = ni;
			if(oldNI != null)
				oldNI.setConnection(null);
			if(nodeInformation != null)
				nodeInformation.setConnection(this);
		}
	}
	*/
	public String getRemoteAddress()
	{
		return socket.getInetAddress().getHostAddress();
	}
	
	/*
	public Address getAddress()
	{
		return address;
	}
	*/
	public NodeInformation getNodeInformation()
	{
		return nodeInformation;
	}

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
			byte[] bytes = msg.getEncodedMessage();
			os.write(0x7E);
			os.write(bytes.length & 0x00FF);
			os.write((bytes.length >> 8) & 0x00FF);
			os.write(bytes);
			os.write(msg.getCRC());
			os.flush();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void close()
	{
		
		/*if(nodeInformation != null)
			nodeInformation.setConnection(null);
		if(address != null)
			address.setConnection(null);
		*/
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
