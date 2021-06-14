package io.firebus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import io.firebus.interfaces.ConnectionListener;

public class Connection extends Thread 
{
	public static int STATE_NEW = 0;
	public static int STATE_INITIALIZING = 1;
	public static int STATE_INITIALIZED = 2;
	public static int STATE_ACTIVE = 3;
	public static int STATE_FAILED = 4;
	public static int STATE_CLOSING = 5;
	public static int STATE_DEAD = 6;
	
	private Logger logger = Logger.getLogger("io.firebus");
	protected int state;
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
		logger.fine("Initialising received connection from " + s.getRemoteSocketAddress());
		state = STATE_NEW;
		socket = s;
		listener = cl;
		networkName = net;
		secretKey = k;
		localNodeId = nid;
		localPort = p;
		setName("fbConn" + getId());
		start();
	}
	
	public Connection(Address a, String net, SecretKey k, int nid, int p, ConnectionListener cl) 
	{
		logger.fine("Initialising connection to " + a);
		state = STATE_NEW;
		remoteAddress = a;
		listener = cl;
		networkName = net;
		secretKey = k;
		localNodeId = nid;
		localPort = p;
		setName("fbConn" + getId());
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
		return state == STATE_ACTIVE;
	}
	
	public void run()
	{
		byteCount = 0;
		timeMark = System.currentTimeMillis();
		load = 0;
		initialise();
		if(state == STATE_INITIALIZED)
		{
			listener.connectionCreated(this);
			state = STATE_ACTIVE;
			synchronized(this) {
				notifyAll();
			}			
			listening();
		}
		else
		{
			listener.connectionFailed(this);
		}
		listener.connectionClosed(this);
		state = STATE_DEAD;
	}
	
	protected void initialise()
	{
		try
		{
			state = STATE_INITIALIZING;
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

					byte[] checkBytes = {(byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE};
					os.write(checkBytes);
					byte[] remoteCheckBytes = new byte[4];
					is.read(remoteCheckBytes);
					
					if(Arrays.equals(checkBytes, remoteCheckBytes)) {
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
						if(remoteNodeId != localNodeId) {
							logger.fine("Established connection " + getId() + " with node " + remoteNodeId + " at address " + remoteAddress);
							state = STATE_INITIALIZED;						
						} else {
							logger.info("Tried to establish connection " + getId() + " with self at address " + remoteAddress);
							state = STATE_FAILED;
						}						
					} else {
						logger.info("Tried to establish connection " + getId() + ", but received a bad cipher");
						state = STATE_FAILED;
					}
				}
				else
				{
					logger.fine("Firebus network mismatch");
					close();
				}
			}
			else
			{
				logger.fine("Socket not connected for connection " + getId());
			}
		}
		catch(Exception e)
		{
			logger.severe("Error trying to establish connection with " + remoteAddress + " : " + e.getMessage());
			close();
		}		
	}
	
	protected void listening()
	{
		msgState = 0;
		while(state == STATE_ACTIVE)
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
						if(listener != null && message != null)
							listener.messageReceived(message, this);
					}
					else
					{
						logger.fine("Received corrupted message from connection " + getId() + " from node id " + remoteNodeId);
					}
					msgState = 0;
				}
			} 
			catch (Exception e) 
			{
				if(state == STATE_ACTIVE) 
				{
					logger.severe("Exception on connection : " + e.getMessage());
					close();
				}
			}
		}		
	}
	
	public synchronized void sendMessage(Message msg)
	{
		while(state == STATE_NEW || state == STATE_INITIALIZING || state == STATE_INITIALIZED) {
			try { wait();} catch(Exception e) {}
		}
		
		if(state == STATE_ACTIVE)
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
				int crc = 0;
				for(int i = 0; i < bytes.length; i++)
					crc = (crc ^ bytes[i]) & 0x00FF;
				os.write(crc);
				os.flush();
				byteCount += bytes.length;
				logger.finer("Sent message on connection " + getId() + " to remote node " + remoteNodeId + "(load: " + load + ")");
			}
			catch(Exception e)
			{
				logger.severe("Exception on connection while sending message : " + e.getMessage());
				close();
			}
		}
	}
	
	public void close()
	{
		try 
		{
			state = STATE_CLOSING;
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
