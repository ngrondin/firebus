package io.firebus;

import java.nio.ByteBuffer;

import io.firebus.exceptions.MessageException;


public class Message 
{
	protected byte[] encodedMessage;
	protected short version;
	protected int messageId;
	protected int originatorId;
	protected int destinationId;
	protected int flags;
	protected int repeatCount;
	protected int repeatsLeft;
	protected int type;
	protected int correlation;
	protected int correlationSequence;
	protected String subject;
	protected Payload payload;


	public static final int MSGTYPE_QUERYNODE = 1;
	public static final int MSGTYPE_NODEINFORMATION = 2;
	public static final int MSGTYPE_GETFUNCTIONINFORMATION = 4;
	public static final int MSGTYPE_FUNCTIONINFORMATION = 5;
	public static final int MSGTYPE_REQUESTSERVICE = 6;
	public static final int MSGTYPE_REQUESTSERVICEANDFORGET = 19;
	public static final int MSGTYPE_SERVICERESPONSE = 7;
	public static final int MSGTYPE_FUNCTIONUNAVAILABLE = 8;
	public static final int MSGTYPE_SERVICEERROR = 9;
	public static final int MSGTYPE_PROGRESS = 10;
	public static final int MSGTYPE_PUBLISH = 11;
	public static final int MSGTYPE_REPUBLISH = 12;
	public static final int MSGTYPE_REQUESTSTREAM = 14;
	public static final int MSGTYPE_STREAMACCEPT = 15;
	public static final int MSGTYPE_STREAMERROR = 16;
	public static final int MSGTYPE_STREAMDATA = 17;
	public static final int MSGTYPE_STREAMEND = 18;
	
	
	protected static final short  MESSAGE_VERSION = 5;
	
	protected static int nextId = 0;
	
	public Message(int d, int o, int t, String s, Payload p)
	{
		version = MESSAGE_VERSION;
		messageId = getNextId();
		destinationId = d;
		originatorId = o;
		flags = 0;
		repeatCount = 0;
		repeatsLeft = 10;
		type = t;
		correlation = 0;
		correlationSequence = 0;
		subject = s;
		payload = p;
	}
	

	private Message(int i, int d, int o, int f, int rc, int rl, int t, int c, int cs, String s, Payload p)
	{
		version = MESSAGE_VERSION;
		messageId = i;
		destinationId = d;
		originatorId = o;
		flags = f;
		repeatCount = rc;
		repeatsLeft = rl;
		type = t;
		correlation = c;
		correlationSequence = cs;
		subject = s;
		payload = p;
	}
	
	private static synchronized int getNextId() 
	{
		return nextId++;
	}
	
	public Message repeat()
	{
		Message msg = new Message(messageId, destinationId, originatorId, flags, repeatCount + 1, repeatsLeft - 1, type, correlation, correlationSequence, subject, payload);
		return msg;
	}
	
	public static Message deserialise(byte[] encodedMessage) throws MessageException
	{
		ByteBuffer bb = ByteBuffer.wrap(encodedMessage);
		short version = bb.getShort();
		if(version == MESSAGE_VERSION)
		{
			int messageId = bb.getInt();
			int destinationId = bb.getInt();
			int originatorId = bb.getInt();
			int flags = bb.getInt();
			int repeatCount = bb.get();
			int repeatsLeft = bb.get();
			int type = bb.get();
			int correlation = bb.getInt();
			int correlationSequence = bb.getInt();
			int subjectLen = bb.getInt();
			String subject = new String(encodedMessage, bb.position(), subjectLen);
			bb.position(bb.position() + subjectLen);
			byte[] payloadBytes = new byte[bb.remaining()];
			bb.get(payloadBytes);
			Payload payload = Payload.deserialise(payloadBytes);
			return new Message(messageId, destinationId, originatorId, flags, repeatCount, repeatsLeft, type, correlation, correlationSequence, subject, payload);			
		}
		else
		{
			throw new MessageException("Wrong message version");
		}
	}
	
	public byte[] serialise()
	{
		byte[] payloadBytes = null;
		if(payload != null)
			payloadBytes = payload.serialise();
		int len = 33;
		if(subject != null)
			len += subject.length();
		if(payloadBytes != null)
			len += payloadBytes.length;
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
		bb.putInt(correlationSequence);
		if(subject != null)
		{
			bb.putInt(subject.length());
			bb.put(subject.getBytes(), 0, subject.length());
		}
		else
		{
			bb.putInt(0);
		}

		if(payloadBytes != null)
		{
			bb.put(payloadBytes);	
		}
		return bb.array();
	}
	
	
	public void setRepeatsLeft(int rl)
	{
		repeatsLeft = rl;
	}
	
	public void setCorrelation(int c, int s)
	{
		correlation = c;
		correlationSequence = s;
	}
	
	public void setCorrelationSequence(int s)
	{
		correlationSequence = s;
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
	
	public String getHeaderString() 
	{
		return getTypeString() + ":" + subject + "@" + destinationId;
	}
	
	public String getTypeString()
	{
		String ret = null;
		if(type == Message.MSGTYPE_QUERYNODE)
			ret = "Query Node";
		else if(type == Message.MSGTYPE_NODEINFORMATION)
			ret = "Node Information";
		else if(type == Message.MSGTYPE_GETFUNCTIONINFORMATION)
			ret = "Get Function Information";
		else if(type == Message.MSGTYPE_FUNCTIONINFORMATION)
			ret = "Function Information";
		else if(type == Message.MSGTYPE_REQUESTSERVICE)
			ret = "Request Service";
		else if(type == Message.MSGTYPE_SERVICERESPONSE)
			ret = "Service Response";
		else if(type == Message.MSGTYPE_FUNCTIONUNAVAILABLE)
			ret = "Service Unavailable";
		else if(type == Message.MSGTYPE_SERVICEERROR)
			ret = "Service Error";
		else if(type == Message.MSGTYPE_PROGRESS)
			ret = "Service In Progress";
		else if(type == Message.MSGTYPE_PUBLISH)
			ret = "Publish";
		else if(type == Message.MSGTYPE_REPUBLISH)
			ret = "Republish";
		else if(type == Message.MSGTYPE_REQUESTSTREAM)
			ret = "Stream Request";
		else if(type == Message.MSGTYPE_STREAMACCEPT)
			ret = "Stream Accept";
		else if(type == Message.MSGTYPE_STREAMERROR)
			ret = "Stream Error";
		else if(type == Message.MSGTYPE_STREAMDATA)
			ret = "Stream Data";
		else if(type == Message.MSGTYPE_STREAMEND)
			ret = "Stream End";		
		else
			ret = "Unknown";
		return ret;
	}
	
	public int getOriginatorId()
	{
		return originatorId;
	}

	public int getDestinationId()
	{
		return destinationId;
	}
	
	public int getCorrelation()
	{
		return correlation;
	}
	
	public int getCorrelationSequence()
	{
		return correlationSequence;
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

	
	public Payload getPayload()
	{
		return payload;
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Message Id   : " + messageId + "\r\n");
		sb.append("Destination  : " + destinationId + "\r\n");
		sb.append("Originator   : " + originatorId + "\r\n");
		sb.append("Repeat Count : " + repeatCount + "\r\n");
		sb.append("Repeats Left : " + repeatsLeft + "\r\n");
		sb.append("Type         : " + getTypeString() + "\r\n");
		sb.append("Correlation  : " + correlation + "\r\n");
		sb.append("Corr. Seq.   : " + correlationSequence + "\r\n");
		sb.append("Subject      : ");
		if(subject != null)
			sb.append(subject);
		sb.append("\r\n");
		if(payload != null)
			sb.append(payload);
		return sb.toString();
	}
}
