package com.nic.firebus;

import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Payload
{
	public byte[] data;
	public HashMap<String, String> metadata;
	
	public Payload(HashMap<String, String> md, byte[] d)
	{
		if(md != null)
			metadata = md;
		else
			metadata = new HashMap<String, String>();
		data = d;
	}
	
	public Payload(byte[] encodedMessage)
	{
		ByteBuffer bb = ByteBuffer.wrap(encodedMessage);
		int metadataLen = bb.getInt();
		String metadataStr = new String(encodedMessage, bb.position(), metadataLen);
		bb.position(bb.position() + metadataLen);
		
		data = new byte[bb.remaining()];
		System.arraycopy(encodedMessage, bb.position(), data, 0, bb.remaining());
		
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
	
	public byte[] encode()
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
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(metadata.toString());
		if(data != null)
			sb.append(new String(data));
		return sb.toString();
	}
}
