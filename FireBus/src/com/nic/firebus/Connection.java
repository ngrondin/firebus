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

import com.nic.firebus.exceptions.ConnectionException;
import com.nic.firebus.interfaces.ConnectionListener;

public class Connection extends Thread 
{
	private Logger logger = Logger.getLogger("com.nic.firebus");
	protected Socket socket;
	protected InputStream is;
	protected OutputStream os;
	protected ConnectionListener listener;
	protected int remoteNodeId;
	protected Address remoteAddress;
	protected IvParameterSpec IV;
	protected Cipher encryptionCipher;
	protected Cipher decryptionCipher;
	protected boolean quit;
	protected int msgState;
	protected int msgLen;
	protected int msgPos;
	protected int msgCRC;
	protected byte[] msg;
	
	public Connection(Socket s, String net, SecretKey k, int nid, int p, ConnectionListener cl) throws IOException, ConnectionException
	{
		socket = s;
		initialise(net, k, nid, p, cl);
	}
	
	public Connection(Address a, String net, SecretKey k, int nid, int p, ConnectionListener cl) throws UnknownHostException, IOException, ConnectionException
	{
		socket = new Socket(a.getIPAddress(), a.getPort());
		remoteAddress = a;
		initialise(net, k, nid, p, cl);
	}
	
	protected void initialise(String net, SecretKey k, int nid, int p, ConnectionListener cl) throws IOException, ConnectionException
	{
		logger.fine("Initialising Connection");
		listener = cl;
		
		is = socket.getInputStream();
		os = socket.getOutputStream();
		
		os.write(net.length());
		os.write(net.getBytes());
		int netNameLen = is.read();
		byte[] netNameBytes = new byte[netNameLen];
		is.read(netNameBytes);
		String remoteNetName = new String(netNameBytes);
		if(!remoteNetName.equals(net))
			throw new ConnectionException("Remote node is not on the same Firebus network");

		try
		{
			encryptionCipher = Cipher.getInstance("AES/CFB8/NoPadding");
			encryptionCipher.init(Cipher.ENCRYPT_MODE, k);
			os.write(encryptionCipher.getIV());
			byte[] remoteIVBytes = new byte[16];
			is.read(remoteIVBytes);
			IvParameterSpec remoteIV = new IvParameterSpec(remoteIVBytes);
			decryptionCipher = Cipher.getInstance("AES/CFB8/NoPadding");
			decryptionCipher.init(Cipher.DECRYPT_MODE, k, remoteIV);
			os = new CipherOutputStream(os, encryptionCipher);
			is = new CipherInputStream(is, decryptionCipher);
		}
		catch(IOException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException e)
		{
			logger.severe("Connection encryption setup failed : " + e.getMessage());
			throw new ConnectionException(e.getMessage());
		}
		
		os.write(ByteBuffer.allocate(4).putInt(nid).array());
		os.write(socket.getLocalAddress().getAddress());
		os.write(ByteBuffer.allocate(4).putInt(p).array());

		byte[] ab = new byte[4];
		is.read(ab);
		remoteNodeId = (ByteBuffer.wrap(ab)).getInt();
		is.read(ab);
		InetAddress a = InetAddress.getByAddress(ab);
		is.read(ab);
		int remotePort = (ByteBuffer.wrap(ab)).getInt();
		Address advertisedRemoteAddress = new Address(a.getHostAddress(), remotePort);
		if(remoteAddress == null)
		{
			if(advertisedRemoteAddress.getIPAddress().equals(socket.getInetAddress().getHostAddress()))
			{
				remoteAddress = advertisedRemoteAddress;
			}
		}
		
		logger.info("Established connection with node " + remoteNodeId + " at address " + remoteAddress);
		quit = false;
		msgState = 0;
		setName("Firebus Connection");
		listener.connectionCreated(this);
		start();
	}

	
	public Address getRemoteAddress()
	{
		return remoteAddress;
	}
	
	public int getRemoteNodeId()
	{
		return remoteNodeId;
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
						Message message = Message.deserialise(msg);
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
			byte[] bytes = msg.serialise();
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
