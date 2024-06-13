package io.firebus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import io.firebus.data.DataMap;
import io.firebus.interfaces.ConnectionListener;
import io.firebus.logging.Logger;
import io.firebus.utils.Queue;

public class Connection extends Thread 
{
	public static int STATE_NEW = 0;
	public static int STATE_INITIALIZING = 1;
	public static int STATE_INITIALIZED = 2;
	public static int STATE_ACTIVE = 3;
	public static int STATE_FAILED = 4;
	public static int STATE_CLOSING = 5;
	public static int STATE_DEAD = 6;
	
	protected int state;
	protected Socket socket;
	protected String networkName;
	protected SecretKey secretKey;
	protected InputStream is;
	protected OutputStream os;
	protected Queue<Message> queue;
	protected ConnectionListener listener;
	protected int localNodeId;
	protected int localPort;
	protected int remoteNodeId;
	protected Address remoteAddress;
	protected IvParameterSpec IV;
	protected Cipher encryptionCipher;
	protected Cipher decryptionCipher;
	protected long initTime;
	protected long timeMark;
	protected long sentCount;
	protected long sentBytes;
	protected long sentLast;
	protected long recvCount;
	protected long recvBytes;
	protected long recvLast;
	protected int load;
	protected Thread sendThread;
	
	public Connection(Socket s, String net, SecretKey k, int nid, int p, ConnectionListener cl) 
	{
		Logger.fine("fb.connection.init.from", new DataMap("remote", s.getRemoteSocketAddress()));
		constructorSetup();
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
		Logger.fine("fb.connection.init.to", new DataMap("to", a));
		constructorSetup();
		remoteAddress = a;
		listener = cl;
		networkName = net;
		secretKey = k;
		localNodeId = nid;
		localPort = p;
		start();
	}
	
	private void constructorSetup() 
	{
		state = STATE_NEW;
		setName("fbConn" + getId() + "Recv");
		queue = new Queue<Message>(100);
		sentCount = 0;
		sentBytes = 0;
		recvCount = 0;
		recvBytes = 0;
		initTime = System.currentTimeMillis();
		timeMark = initTime;
		load = 0;
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
			load = (int)(1000 * (long)sentCount / delta);
		else
			load = 0;
		timeMark = now;
		sentCount = 0;
		return load;
	}

	public boolean isReady()
	{
		return state == STATE_ACTIVE;
	}
	
	public boolean isHealthy()
	{
		if(state == STATE_FAILED || state == STATE_CLOSING || state == STATE_DEAD) return false;
		if(queue.getDepth() > 2 && sentLast < System.currentTimeMillis() - 10000) return false;
		return true;
	}
	
	public void run()
	{
		initialise();
		if(state == STATE_INITIALIZED)
		{
			state = STATE_ACTIVE;
			listener.connectionCreated(this);
			sendThread = new Thread(new Runnable() {
				public void run() {
					send();
				}
			});
			sendThread.start();
			listen();
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
				socket.setTcpNoDelay(true);
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
							Logger.info("fb.connection.established", new DataMap("id", getId(), "node", remoteNodeId, "address", remoteAddress));
							state = STATE_INITIALIZED;						
						} else {
							Logger.info("fb.connection.withself", new DataMap("id", getId(), "address", remoteAddress));
							state = STATE_FAILED;
						}						
					} else {
						Logger.warning("fb.connection.badcipher", new DataMap("id", getId()));
						state = STATE_FAILED;
					}
				}
				else
				{
					Logger.info("fb.connection.badnetwork", new DataMap("id", getId()));
					close();
				}
			}
			else
			{
				Logger.warning("fb.connection.socketnotconnected", new DataMap("id", getId()));
			}
		}
		catch(Exception e)
		{
			Logger.warning("fb.connection.errorconnecting", new DataMap("id", getId(), "remote", remoteAddress, "message", e.getMessage()));
			close();
		}		
	}
	
	protected void listen()
	{
		int msgState = 0;
		int msgLen = 0;
		int msgPos = 0;
		int msgCRC = 0;
		byte[] msgData = null;
		byte[] inBuf = new byte[1024];
		while(state == STATE_ACTIVE) {
			try  {
				int r = is.read(inBuf, 0, 1024);
				if(r > -1) {
					recvBytes += r;
					for(int j = 0; j < r; j++) {
						int i = ((int)inBuf[j]) & 0xff;
						if(msgState == 0)
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
								msgData = new byte[msgLen];
								msgPos = 0;
								msgCRC = 0;
								msgState = 2;
							}
						}
						else if(msgState == 2)
						{
							msgData[msgPos] = (byte)i;
							msgCRC = (msgCRC ^ i) & 0x00FF;
							msgPos++;
							if(msgPos >= msgLen)
								msgState = 3;
						}
						else if(msgState == 3)
						{
							if(i == msgCRC)
							{
								Message msg = Message.deserialise(msgData);
								if(listener != null && msg != null)
									listener.messageReceived(msg, this);
								recvCount++;
								recvLast = System.currentTimeMillis();
							}
							else
							{
								Logger.severe("fb.connection.badcrc", new DataMap("id", getId(), "node", remoteNodeId));
								close();
							}
							msgState = 0;
						} else {
							Logger.severe("fb.connection.badrecevingstate", new DataMap("id", getId(), "node", remoteNodeId));
							close();
						}
					}
				} else {
					close();
				}
			} 
			catch (Exception e) 
			{
				if(state == STATE_ACTIVE) 
				{
					Logger.severe("fb.connection.exception", new DataMap("id", getId()), e);
					close();
				}
			}
		}		
	}
	
	protected void send() 
	{
		Thread.currentThread().setName("fbConn" + getId() + "Send");
		while(state == STATE_ACTIVE)
		{
			try {
				Message msg = queue.popWait();
				if(msg != null) {
					try {
						byte[] msgData = msg.serialise();
						int crc = 0;
						for(int i = 0; i < msgData.length; i++)
							crc = (crc ^ msgData[i]) & 0x00FF;
						int packetSize = msgData.length + 6;
						packetSize += 16 - (packetSize % 16);
						byte[] bytes = new byte[packetSize];
						bytes[0] = 0x7E;
						bytes[1] = (byte)(msgData.length & 0x000000FF);
						bytes[2] = (byte)((msgData.length >> 8) & 0x000000FF);
						bytes[3] = (byte)((msgData.length >> 16) & 0x000000FF);
						bytes[4] = (byte)((msgData.length >> 24) & 0x000000FF);
						for(int i = 0; i < msgData.length; i++) 
							bytes[5 + i] = msgData[i];
						bytes[msgData.length + 5] = (byte)crc;
						os.write(bytes);
						sentCount++;
						sentBytes += bytes.length;
						sentLast = System.currentTimeMillis();
					} catch(Exception e1) {
						Logger.severe("fb.connection.sending", new DataMap("id", getId()), e1);
						close();
					}
				}
			} catch(Exception e) {
				Logger.severe("fb.connection.sending", new DataMap("id", getId()), e);
			}
		}		
	}
	
	public void sendMessage(Message msg)
	{
		queue.push(msg);
	}
	
	public void close()
	{
		try 
		{
			Logger.info("fb.connection.closing", new DataMap("id", getId()));
			state = STATE_CLOSING;
			if(socket != null)
				socket.close();
			if(is != null)
				is.close();
			if(os != null)
				os.close();
			if(sendThread != null && sendThread.isAlive())
				sendThread.interrupt();
		} 
		catch (IOException e) 
		{
			Logger.severe("fb.connection.closing", new DataMap("id", getId()), e);
		}
	}
	
	public String toString()
	{
		return "Connection " + getId() + " to node " + remoteNodeId + " at " + remoteAddress;
	}
	
	public DataMap getStatus()
	{
		long now = System.currentTimeMillis();
		DataMap status = new DataMap();
		status.put("remoteNodeId", remoteNodeId);
		status.put("remoteAddress", remoteAddress != null ? remoteAddress.toString() : null);
		status.put("startedSince", (now - initTime));
		status.put("sent", new DataMap("count", sentCount, "bytes", sentBytes, "last", now - sentLast));
		status.put("recv", new DataMap("count", recvCount, "bytes", recvBytes, "last", now - recvLast));
		status.put("state", state == STATE_NEW ? "new" : state == STATE_INITIALIZING ? "initializing" : state == STATE_INITIALIZED ? "initialized" : state == STATE_ACTIVE ? "active" : state == STATE_FAILED ? "failed" : state == STATE_CLOSING ? "closing" : state == STATE_DEAD ? "dead" : "unknown");
		status.put("queue", queue.getStatus());
		return status;
	}
	
}
