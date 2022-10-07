package io.firebus;

import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import io.firebus.data.DataException;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;

public class Payload
{
	protected byte[] dataBytes;
	protected String dataString;
	protected DataMap dataMap;
	protected DataList dataList;
	
	public final static int TYPE_NULL = 0;
	public final static int TYPE_BYTES = 1;
	public final static int TYPE_STRING = 2;
	public final static int TYPE_DATAMAP = 3;
	public final static int TYPE_DATALIST = 4;
	
	public HashMap<String, String> metadata  = new HashMap<String, String>();

	public Payload()
	{
	}

	public Payload(byte[] d)
	{
		dataBytes = d;
	}
	
	public Payload(String s)
	{
		dataString = s;
	}
	
	public Payload(DataMap m)
	{
		dataMap = m;
	}
	
	public Payload(DataList l)
	{
		dataList = l;
	}
	
	public Payload(HashMap<String, String> md, byte[] d)
	{
		if(md != null)
			metadata = md;
		dataBytes = d;
	}
	
	public byte[] serialise()
	{
		byte[] metaBytes = metadata.toString().getBytes();
		byte[] bytes = getBytes();
		int type = getDataType();
		ByteBuffer bb = ByteBuffer.allocate(4 + metaBytes.length + 4 +  (bytes != null ? bytes.length : 0));
		bb.putInt(metaBytes.length);
		bb.put(metaBytes, 0, metaBytes.length);
		bb.putInt(type);
		if(bytes != null)
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
			if(type == TYPE_BYTES)
			{
				payload.setData(bytes);
			}
			else if(type == TYPE_STRING) 
			{
				payload.setData(new String(bytes));
			}
			else if(type == TYPE_DATAMAP)
			{
				try {
					DataMap map = new DataMap(new String(bytes));
					payload.setData(map);
				} catch(DataException e) {
					payload.setData(bytes);
				}
			} else if(type == TYPE_DATALIST) {
				try {
					DataList list = new DataList(new String(bytes));
					payload.setData(list);
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
	
	public int getDataType()
	{
		if(dataBytes != null)
			return TYPE_BYTES;
		else if(dataString != null) 
			return TYPE_STRING;
		else if(dataMap != null)
			return TYPE_DATAMAP;
		else if(dataList != null)
			return TYPE_DATALIST;
		else 
			return 0;
	}
	
	public Object getDataObject()
	{
		if(dataBytes != null)
			return dataBytes;
		else if(dataString != null) 
			return dataString;
		else if(dataMap != null)
			return dataMap;
		else if(dataList != null)
			return dataList;
		else 
			return null;
	}
	
	public byte[] getBytes()
	{
		if(dataBytes != null)
			return dataBytes;
		else if(dataString != null)
			return dataString.getBytes();
		else if(dataMap != null)
			return dataMap.toString().getBytes();
		else if(dataList != null)
			return dataList.toString().getBytes();
		else 
			return null;
	}
	
	public String getString()
	{
		if(dataBytes != null)
			return new String(dataBytes);
		else if(dataString != null)
			return dataString;
		else if(dataMap != null)
			return dataMap.toString();
		else if(dataList != null)
			return dataList.toString();
		else 
			return null;
	}
	
	public DataMap getDataMap() throws DataException
	{
		if(dataMap != null) 
			return dataMap;
		else if(dataBytes != null)
			return new DataMap(new String(dataBytes));
		else if(dataString != null)
			return new DataMap(dataString);
		else
			throw new DataException("Not a DataMap type");
	}
	
	public DataList getDataList() throws DataException
	{
		if(dataList != null) 
			return dataList;
		else if(dataBytes != null)
			return new DataList(new String(dataBytes));
		else if(dataString != null)
			return new DataList(dataString);
		else
			throw new DataException("Not a DataList type");
	}
	
	public void setData(byte[] bytes)
	{
		dataBytes = bytes;
	}
	
	public void setData(String s)
	{
		dataString = s;
	}
	
	public void setData(DataMap dm) 
	{
		dataMap = dm;
	}
	
	public void setData(DataList dl) 
	{
		dataList = dl;
	}
	
	public String toString()
	{
		return getString();
	}
}
