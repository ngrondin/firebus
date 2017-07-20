package com.nic.firebus;

import java.nio.ByteBuffer;

public class Message 
{
	protected Connection connection;
	protected byte[] encodedMessage;
	protected boolean decoded;
	protected boolean encoded;
	protected int type;
	protected int originator;
	protected int repeater;
	protected int destination;
	protected String subject;
	protected byte[] payload;
	
	public static int MSGTYPE_QUERY = 0;
	public static int MSGTYPE_ADVERTISE = 1;
	public static int MSGTYPE_REQUEST = 2;
	public static int MSGTYPE_RESPONSE = 3;
	public static int MSGTYPE_PUBLISH = 4;
	public static int MSGTYPE_RECALL = 5;
	
	public Message(byte[] b, Connection c)
	{
		encodedMessage = b;
		connection = c;
		decoded = false;
		encoded = true;
	}
	
	public Message(int t, int o, int r, int d, String s, byte[] p)
	{
		type = t;
		originator = o;
		repeater = r;
		destination = d;
		subject = s;
		payload = p;
		decoded = true;
		encoded = false;
	}
	
	public void decode()
	{
		ByteBuffer bb = ByteBuffer.wrap(encodedMessage);
		type = bb.getInt();
		originator = bb.getInt();
		repeater = bb.getInt();
		destination = bb.getInt();
		int subjectLen = bb.getInt();
		subject = new String(encodedMessage, bb.position(), subjectLen);
		bb.position(bb.position() + subjectLen);
		System.arraycopy(encodedMessage, bb.position(), payload, 0, bb.remaining());
		decoded = true;
	}
	
	public void encode()
	{
		int len = 20;
		if(subject != null)
			len += subject.length();
		if(payload != null)
			len += payload.length;
		ByteBuffer bb = ByteBuffer.allocate(len);
		bb.putInt(type);
		bb.putInt(originator);
		bb.putInt(repeater);
		bb.putInt(destination);
		if(subject != null)
		{
			bb.putInt(subject.length());
			bb.put(subject.getBytes(), 0, subject.length());
		}
		else
		{
			bb.putInt(0);
		}
		if(payload != null)
		{
			bb.put(payload);	
		}
		encodedMessage = bb.array();
		encoded = true;
	}

	public int getType()
	{
		return type;
	}
	
	public int getOriginator()
	{
		return originator;
	}
	
	public int getRepeater()
	{
		return repeater;
	}
	
	public int getDestination()
	{
		return destination;
	}
	
	public String getSubject()
	{
		return subject;
	}
	
	public byte[] getPayload()
	{
		return payload;
	}
	
	public Connection getConnection()
	{
		return connection;
	}
	
	public byte[] getEncodedMessage()
	{
		if(!encoded)
			encode();
		return encodedMessage;
	}
}
