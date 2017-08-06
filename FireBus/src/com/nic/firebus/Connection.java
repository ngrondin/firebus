package com.nic.firebus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.nic.firebus.interfaces.ConnectionListener;

public class Connection extends Thread 
{
	private Logger logger = Logger.getLogger(Connection.class.getName());
	protected Socket socket;
	protected InputStream is;
	protected OutputStream os;
	//protected CipherInputStream cis;
	//protected CipherOutputStream cos;
	protected ConnectionListener listener;
	protected boolean quit;
	protected int msgState;
	protected int msgLen;
	protected int msgPos;
	protected int msgCRC;
	protected byte[] msg;
	protected SecretKey secretKey;
	protected IvParameterSpec IV;
	protected Cipher encryptionCipher;
	protected Cipher decryptionCipher;
	
	public Connection(Socket s, ConnectionListener cl, String key) throws IOException
	{
		socket = s;
		initialise(cl, key);
	}
	
	public Connection(Address a, ConnectionListener cl, String key) throws UnknownHostException, IOException
	{
		socket = new Socket(a.getIPAddress(), a.getPort());
		initialise(cl, key);
	}
	
	protected void initialise(ConnectionListener cl, String key) throws IOException
	{
		is = socket.getInputStream();
		os = socket.getOutputStream();
		listener = cl;
		quit = false;
		msgState = 0;
		setName("Firebus Connection");

		try
		{
			secretKey = new SecretKeySpec(key.getBytes(), "AES");
			encryptionCipher = Cipher.getInstance("AES/CFB8/NoPadding");
			encryptionCipher.init(Cipher.ENCRYPT_MODE, secretKey);
			os.write(encryptionCipher.getIV());
			byte[] remoteIVBytes = new byte[16];
			is.read(remoteIVBytes);
			IvParameterSpec remoteIV = new IvParameterSpec(remoteIVBytes);
			decryptionCipher = Cipher.getInstance("AES/CFB8/NoPadding");
			decryptionCipher.init(Cipher.DECRYPT_MODE, secretKey, remoteIV);
			os = new CipherOutputStream(os, encryptionCipher);
			is = new CipherInputStream(is, decryptionCipher);
		}
		catch(Exception e)
		{
			logger.severe("Connection encryption setup failed : " + e.getMessage());
		}
		
		logger.fine("Connection Initialised");
		start();
	}

	
	public String getRemoteAddress()
	{
		return socket.getInetAddress().getHostAddress();
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
	
	public String toString()
	{
		return "" + this.getId();
	}
}
