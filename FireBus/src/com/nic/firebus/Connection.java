package com.nic.firebus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.nic.firebus.exceptions.ConnectionException;

public class Connection extends Thread 
{
	private Logger logger = Logger.getLogger(Connection.class.getName());
	protected Socket socket;
	protected InputStream is;
	protected OutputStream os;
	protected NodeCore nodeCore;
	protected String networkName;
	protected int remoteId;
	protected Address remoteAddress;
	protected SecretKey secretKey;
	protected IvParameterSpec IV;
	protected Cipher encryptionCipher;
	protected Cipher decryptionCipher;
	protected boolean quit;
	protected int msgState;
	protected int msgLen;
	protected int msgPos;
	protected int msgCRC;
	protected byte[] msg;
	
	public Connection(Socket s, NodeCore nc, String net, String key) throws IOException, ConnectionException
	{
		socket = s;
		initialise(nc, net, key);
		byte[] ab = new byte[4];
		is.read(ab);
		InetAddress a = InetAddress.getByAddress(ab);
		int remotePort = (is.read() << 8);
		remotePort |= is.read();
		remoteAddress = new Address(a.getHostAddress(), remotePort);
		nodeCore.connectionCreated(this);
		start();
	}
	
	public Connection(Address a, NodeCore nc, String net, String key, int listeningPort) throws UnknownHostException, IOException, ConnectionException
	{
		socket = new Socket(a.getIPAddress(), a.getPort());
		remoteAddress = a;
		initialise(nc, net, key);
		os.write(socket.getInetAddress().getAddress());
		os.write((listeningPort >> 8) & 0x00FF);
		os.write(listeningPort  & 0x00FF);
		start();
	}
	
	protected void initialise(NodeCore nc, String net, String key) throws IOException, ConnectionException
	{
		is = socket.getInputStream();
		os = socket.getOutputStream();
		networkName = net;
		nodeCore = nc;
		
		os.write(networkName.length());
		os.write(networkName.getBytes());
		int netNameLen = is.read();
		byte[] netNameBytes = new byte[netNameLen];
		is.read(netNameBytes);
		String remoteNetName = new String(netNameBytes);
		if(!remoteNetName.equals(networkName))
			throw new ConnectionException("Remote node is not on the same Firebus network");

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
		catch(IOException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException e)
		{
			logger.severe("Connection encryption setup failed : " + e.getMessage());
			throw new ConnectionException(e.getMessage());
		}
		
		os.write(ByteBuffer.allocate(4).putInt(nodeCore.getNodeId()).array());
		
		logger.fine("Connection Initialised");
		quit = false;
		msgState = 0;
		setName("Firebus Connection");
	}

	
	public Address getRemoteAddress()
	{
		return remoteAddress;
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
						if(nodeCore != null)
							nodeCore.messageReceived(message, this);
					}
					msgState = 0;
				}
			} 
			catch (IOException e) 
			{
				close();
				nodeCore.connectionClosed(this);
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
