package com.nic.firebus;

import java.nio.ByteBuffer;

public class Message 
{
	protected byte[] encodedMessage;
	protected boolean decoded;
	protected boolean encoded;
	protected short version;
	protected int messageId;
	protected int originatorId;
	protected int destinationId;
	protected int flags;
	protected int repeatCount;
	protected int repeatsLeft;
	protected int type;
	protected int correlation;
	protected String subject;
	protected byte[] payload;


	public static final int MSGTYPE_QUERYNODE = 1;
	public static final int MSGTYPE_NODEINFORMATION = 2;
	public static final int MSGTYPE_GETFUNCTIONINFORMATION = 4;
	public static final int MSGTYPE_SERVICEINFORMATION = 5;
	public static final int MSGTYPE_REQUESTSERVICE = 6;
	public static final int MSGTYPE_SERVICERESPONSE = 7;
	public static final int MSGTYPE_SERVICEUNAVAILABLE = 8;
	public static final int MSGTYPE_SERVICEERROR = 9;
	public static final int MSGTYPE_PUBLISH = 10;
	public static final int MSGTYPE_REPUBLISH = 11;
	
	protected static int nextId = 0;
	
	public Message(byte[] b)
	{
		encodedMessage = b;
		decoded = false;
		encoded = true;
	}

	public Message(int d, int o, int t, String s, byte[] p)
	{
		version = 1;
		messageId = nextId++;
		destinationId = d;
		originatorId = o;
		flags = 0;
		repeatCount = 0;
		repeatsLeft = 10;
		type = t;
		correlation = 0;
		subject = s;
		payload = p;
		decoded = true;
		encoded = false;
	}
	
	private Message(int i, int d, int o, int rc, int rl, int t, int c, String s, byte[] p)
	{
		version = 1;
		messageId = i;
		destinationId = d;
		originatorId = o;
		flags = 0;
		repeatCount = rc;
		repeatsLeft = rl;
		type = t;
		correlation = c;
		subject = s;
		payload = p;
		decoded = true;
		encoded = false;		
	}
	
	public Message repeat()
	{
		Message msg = new Message(messageId, destinationId, originatorId, repeatCount + 1, repeatsLeft - 1, type, correlation, subject, payload);
		return msg;
	}
	
	public void decode()
	{
		ByteBuffer bb = ByteBuffer.wrap(encodedMessage);
		version = bb.getShort();
		messageId = bb.getInt();
		destinationId = bb.getInt();
		originatorId = bb.getInt();
		flags = bb.getInt();
		repeatCount = bb.get();
		repeatsLeft = bb.get();
		type = bb.get();
		correlation = bb.getInt();
		int subjectLen = bb.getInt();
		subject = new String(encodedMessage, bb.position(), subjectLen);
		bb.position(bb.position() + subjectLen);
		payload = new byte[bb.remaining()];
		System.arraycopy(encodedMessage, bb.position(), payload, 0, bb.remaining());
		decoded = true;
	}
	
	public void encode()
	{
		int len = 29;
		if(subject != null)
			len += subject.length();
		if(payload != null)
			len += payload.length;
		ByteBuffer bb = ByteBuffer.allocate(len);
		bb.putShort(version);
		bb.putInt(messageId);
		bb.putInt(destinationId);
		bb.putInt(originatorId);
		bb.putInt(flags);
		bb.put((byte)repeatCount);
		bb.put((byte)repeatsLeft);
		bb.put((byte)type);
		bb.putInt(correlation);
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
	
	public void setRepeatsLeft(int rl)
	{
		repeatsLeft = rl;
	}
	
	/*
	public void setConnection(Connection c)
	{
		connection = c;
	}
	*/
	public void setCorrelation(int c)
	{
		correlation = c;
	}
	
	public int getid()
	{
		return messageId;
	}
	
	public long getUniversalId()
	{
		return ((((long)originatorId) << 32) + messageId);
	}

	public int getType()
	{
		return type;
	}
	

	public int getOriginatorId()
	{
		return originatorId;
	}

	/*public int getRepeaterId()
	{
		return repeaterId;
	}*/

	public int getDestinationId()
	{
		return destinationId;
	}
	
	public int getCorrelation()
	{
		return correlation;
	}
	
	public int getRepeatCount()
	{
		return repeatCount;
	}
	
	public int getRepeatsLeft()
	{
		return repeatsLeft;
	}
	
	public String getSubject()
	{
		return subject;
	}
	
	public byte[] getPayload()
	{
		return payload;
	}
	/*
	public Connection getConnection()
	{
		return connection;
	}
	*/
	public byte[] getEncodedMessage()
	{
		if(!encoded)
			encode();
		return encodedMessage;
	}
	
	public int getCRC()
	{
		int crc = 0;
		byte[] b = getEncodedMessage();
		for(int i = 0; i < b.length; i++)
		{
			crc = (crc ^ b[i]) & 0x00FF;
		}
		return crc;
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Message Id   : " + messageId + "\r\n");
		sb.append("Destination  : " + destinationId + "\r\n");
		sb.append("Originator   : " + originatorId + "\r\n");
		sb.append("Repeat Count : " + repeatCount + "\r\n");
		sb.append("Repeats Left : " + repeatsLeft + "\r\n");
		sb.append("Type         : ");
		if(type == Message.MSGTYPE_QUERYNODE)
			sb.append("Query Node");
		else if(type == Message.MSGTYPE_NODEINFORMATION)
			sb.append("Node Information");
		else if(type == Message.MSGTYPE_GETFUNCTIONINFORMATION)
			sb.append("Get Function Information");
		else if(type == Message.MSGTYPE_SERVICEINFORMATION)
			sb.append("Function Information");
		else if(type == Message.MSGTYPE_REQUESTSERVICE)
			sb.append("Request Service");
		else if(type == Message.MSGTYPE_SERVICERESPONSE)
			sb.append("Service Response");
		else if(type == Message.MSGTYPE_SERVICEUNAVAILABLE)
			sb.append("Service Unavailable");
		else if(type == Message.MSGTYPE_SERVICEERROR)
			sb.append("Service Error");
		else if(type == Message.MSGTYPE_PUBLISH)
			sb.append("Publish");
		else if(type == Message.MSGTYPE_REPUBLISH)
			sb.append("Republish");
		sb.append("\r\n");
		sb.append("Correlaton   : " + correlation + "\r\n");
		sb.append("Subject      : ");
		if(subject != null)
			sb.append(subject);
		sb.append("\r\n");
		if(payload != null)
			sb.append(new String(payload));
		return sb.toString();
	}
}
