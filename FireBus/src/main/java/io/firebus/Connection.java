package io.firebus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import io.firebus.interfaces.ConnectionListener;

public class Connection extends Thread 
{
	private Logger logger = Logger.getLogger("io.firebus");
	protected Socket socket;
	protected String networkName;
	protected SecretKey secretKey;
	protected InputStream is;
	protected OutputStream os;
	protected ConnectionListener listener;
	protected int localNodeId;
	protected int localPort;
	protected int remoteNodeId;
	protected Address remoteAddress;
	protected IvParameterSpec IV;
	protected Cipher encryptionCipher;
	protected Cipher decryptionCipher;
	protected boolean running;
	protected int msgState;
	protected int msgLen;
	protected int msgPos;
	protected int msgCRC;
	protected byte[] msg;
	protected long timeMark;
	protected int byteCount;
	protected int load;
	
	public Connection(Socket s, String net, SecretKey k, int nid, int p, ConnectionListener cl) 
	{
		logger.fine("Initialising received connection");
		
		socket = s;
		listener = cl;
		networkName = net;
		secretKey = k;
		localNodeId = nid;
		localPort = p;
		start();
	}
	
	public Connection(Address a, String net, SecretKey k, int nid, int p, ConnectionListener cl) 
	{
		logger.fine("Initialising connection to " + a);
		
		remoteAddress = a;
		listener = cl;
		networkName = net;
		secretKey = k;
		localNodeId = nid;
		localPort = p;
		start();
	}

	
	public Address getRemoteAddress()
	{
		return remoteAddress;
	}
	
	public boolean remoteAddressEquals(Address a)
	{
		return remoteAddress != null ? (remoteAddress.equals(a)) : false;
	}
	
	public int getRemoteNodeId()
	{
		return remoteNodeId;
	}
	
	public int getLoad()
	{
		long now = System.currentTimeMillis();
		long delta = now - timeMark;
		if(delta > 0)
			load = (int)(1000 * (long)byteCount / delta);
		else
			load = 0;
		timeMark = now;
		byteCount = 0;
		return load;
	}

	public boolean isReady()
	{
		return running;
	}
	
	public void run()
	{
		running = false;
		byteCount = 0;
		timeMark = System.currentTimeMillis();
		load = 0;
		setName("fbConn" + getId());
		initialise();
		if(running)
		{
			listener.connectionCreated(this);
			listening();
		}
		else
		{
			listener.connectionFailed(this);
		}
		listener.connectionClosed(this);
	}
	
	protected void initialise()
	{
		try
		{
			if(socket == null  &&  remoteAddress != null)
				socket = new Socket(remoteAddress.getIPAddress(), remoteAddress.getPort());

			if(socket != null)
			{
				is = socket.getInputStream();
				os = socket.getOutputStream();
				
				os.write(networkName.length());
				os.write(networkName.getBytes());
				int netNameLen = is.read();
				byte[] netNameBytes = new byte[netNameLen];
				is.read(netNameBytes);
				String remoteNetName = new String(netNameBytes);
				if(remoteNetName.equals(networkName))
				{
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

					os.write(ByteBuffer.allocate(4).putInt(localNodeId).array());
					os.write(socket.getLocalAddress().getAddress());
					os.write(ByteBuffer.allocate(4).putInt(localPort).array());

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

					logger.info("Established connection " + getId() + " with node " + remoteNodeId + " at address " + remoteAddress);
					running = true;
				}
				else
				{
					logger.fine("Firebus network mismatch");
				}
			}
			else
			{
				logger.fine("Socket not connected for connection " + getId());
			}
		}
		catch(Exception e)
		{
			logger.fine(e.getMessage());
		}		
	}
	
	protected void listening()
	{
		msgState = 0;
		while(running)
		{
			try 
			{
				int i = is.read();
				if(i == -1)
				{
					close();
				}
				else if(msgState == 0)
				{
					if(i == 0x7E)
					{
						msgLen = 0;
						msgPos = 0;
						msgState = 1;
					}
				}
				else if(msgState == 1)
				{
					msgLen |= i << (8 * msgPos);
					msgPos++;
					if(msgPos == 4)
					{
						msg = new byte[msgLen];
						msgPos = 0;
						msgCRC = 0;
						msgState = 2;
					}
				}
				else if(msgState == 2)
				{
					msg[msgPos] = (byte)i;
					msgCRC = (msgCRC ^ i) & 0x00FF;
					msgPos++;
					if(msgPos == msgLen)
						msgState = 3;
				}
				else if(msgState == 3)
				{
					if(i == msgCRC)
					{
						Message message = Message.deserialise(msg);
						if(listener != null)
							listener.messageReceived(message, this);
					}
					else
					{
						logger.fine("Received corrupted message from connection " + getId() + " from node id " + remoteNodeId);
					}
					msgState = 0;
				}
			} 
			catch (IOException e) 
			{
				close();
			}
		}		
	}
	
	public synchronized void sendMessage(Message msg)
	{
		if(running)
		{
			try
			{
				byte[] bytes = msg.serialise();
				os.write(0x7E);
				os.write(bytes.length & 0x000000FF);
				os.write((bytes.length >> 8) & 0x000000FF);
				os.write((bytes.length >> 16) & 0x000000FF);
				os.write((bytes.length >> 24) & 0x000000FF);
				os.write(bytes);
				os.write(msg.getCRC());
				os.flush();
				byteCount += bytes.length;
				logger.fine("Sent message on connection " + getId() + " to remote node " + remoteNodeId + "(load: " + load + ")");
			}
			catch(Exception e)
			{
				logger.severe(e.getMessage());
				close();
			}
		}
		else
		{
			if(listener != null)
				listener.connectionClosed(this);
		}
	}
	
	public void close()
	{
		try 
		{
			running = false;
			if(socket != null)
				socket.close();
			if(is != null)
				is.close();
			if(os != null)
				os.close();
		} 
		catch (IOException e) 
		{
			logger.severe(e.getMessage());
		}
	}
	
	public String toString()
	{
		return "Connection " + getId() + " to node " + remoteNodeId + " at " + remoteAddress;
	}
	
}
