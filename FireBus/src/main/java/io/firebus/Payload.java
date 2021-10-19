package io.firebus;

import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import io.firebus.data.DataException;
import io.firebus.data.DataMap;

public class Payload
{
	protected byte[] dataBytes;
	protected DataMap dataMap;
	
	public HashMap<String, String> metadata;

	public Payload()
	{
		metadata = new HashMap<String, String>();
	}

	public Payload(byte[] d)
	{
		metadata = new HashMap<String, String>();
		dataBytes = d;
	}
	
	public Payload(String s)
	{
		metadata = new HashMap<String, String>();
		dataBytes = s.getBytes();
	}
	
	public Payload(DataMap m)
	{
		metadata = new HashMap<String, String>();
		dataMap = m;
	}
	
	public Payload(HashMap<String, String> md, byte[] d)
	{
		if(md != null)
			metadata = md;
		else
			metadata = new HashMap<String, String>();
		dataBytes = d;
	}
	
	public byte[] serialise()
	{
		String metaStr = metadata.toString();
		byte[] bytes = dataBytes != null ? dataBytes : dataMap != null ? dataMap.toString().getBytes() : new byte[0];
		int type = dataBytes != null ? 1 : dataMap != null ? 2 : 0;
		ByteBuffer bb = ByteBuffer.allocate(4 + metaStr.length() + 4 +  bytes.length);
		bb.putInt(metaStr.length());
		bb.put(metaStr.getBytes(), 0, metaStr.length());
		bb.putInt(type);
		bb.put(bytes);
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
			int type = bb.getInt();
			byte[] bytes = new byte[bb.remaining()];
			System.arraycopy(encodedMessage, bb.position(), bytes, 0, bb.remaining());
			
			HashMap<String, String> metadata = new HashMap<String, String>();
			if(metadataLen > 0) {
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
			Payload payload = new Payload();
			payload.metadata = metadata;
			if(type == 1)
			{
				payload.setData(bytes);
			}
			else if(type == 2)
			{
				try {
					DataMap map = new DataMap(new String(bytes));
					payload.setData(map);
				} catch(DataException e) {
					payload.setData(bytes);
				}
			}
			return payload;
		}
		else
		{
			return null;
		}
	}
	
	public byte[] getBytes()
	{
		if(dataBytes != null)
			return dataBytes;
		else if(dataMap != null)
			return dataMap.toString().getBytes();
		else 
			return null;
	}
	
	public String getString()
	{
		if(dataBytes != null)
			return new String(dataBytes);
		else if(dataMap != null)
			return dataMap.toString();
		else 
			return null;
	}
	
	public DataMap getDataMap() throws DataException
	{
		if(dataMap != null) 
			return dataMap;
		else if(dataBytes != null)
			return new DataMap(new String(dataBytes));
		else
			return null;
	}
	
	public void setData(byte[] bytes)
	{
		dataBytes = bytes;
	}
	
	public void setData(String s)
	{
		dataBytes = s.getBytes();
	}
	
	public void setData(DataMap dm) 
	{
		dataMap = dm;
	}
	
	public String toString()
	{
		return getString();
	}
}
