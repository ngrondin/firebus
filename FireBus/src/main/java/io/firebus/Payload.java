package io.firebus;

import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Payload
{
	public byte[] data;
	public HashMap<String, String> metadata;

	public Payload()
	{
		metadata = new HashMap<String, String>();
		data = new byte[0];
	}

	public Payload(byte[] d)
	{
		metadata = new HashMap<String, String>();
		data = d;
	}
	
	public Payload(String s)
	{
		metadata = new HashMap<String, String>();
		data = s.getBytes();
	}
	
	public Payload(HashMap<String, String> md, byte[] d)
	{
		if(md != null)
			metadata = md;
		else
			metadata = new HashMap<String, String>();
		data = d;
	}
	
	public byte[] serialise()
	{
		int len = 4;
		if(!metadata.isEmpty())
			len += metadata.toString().length();
		if(data != null)
			len += data.length;
		ByteBuffer bb = ByteBuffer.allocate(len);
		if(!metadata.isEmpty())
		{
			String metadataStr = metadata.toString();
			bb.putInt(metadataStr.length());
			bb.put(metadataStr.getBytes(), 0, metadataStr.length());
		}
		else
		{
			bb.putInt(0);
		}

		if(data != null)
		{
			bb.put(data);	
		}
		return  bb.array();
	}
	
	public static Payload deserialise(byte[] encodedMessage)
	{
		if(encodedMessage != null  &&  encodedMessage.length > 0)
		{
			ByteBuffer bb = ByteBuffer.wrap(encodedMessage);
			int metadataLen = bb.getInt();
			String metadataStr = new String(encodedMessage, bb.position(), metadataLen);
			bb.position(bb.position() + metadataLen);
			
			byte[] data = new byte[bb.remaining()];
			System.arraycopy(encodedMessage, bb.position(), data, 0, bb.remaining());
			
			HashMap<String, String> metadata = new HashMap<String, String>();
			if(metadataStr.length() > 0) {
				try
				{
					Properties props = new Properties();
					props.load(new StringReader(metadataStr.substring(1, metadataStr.length() - 1).replace(", ", "\n")));       
					for (Map.Entry<Object, Object> e : props.entrySet()) 
					    metadata.put((String)e.getKey(), (String)e.getValue());
				}
				catch(Exception e)
				{}
			}
			
			return new Payload(metadata, data);
		}
		else
		{
			return null;
		}
	}
	
	public byte[] getBytes()
	{
		return data;
	}
	
	public String getString()
	{
		return new String(data);
	}
	
	public void setData(byte[] bytes)
	{
		data = bytes;
	}
	
	public void setData(String s)
	{
		data = s.getBytes();
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(metadata.toString());
		if(data != null)
			sb.append(new String(data));
		return sb.toString();
	}
}
