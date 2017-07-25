package com.nic.utils.ssdp;

import java.util.HashMap;
import java.util.Iterator;

public class SSDPMessage
{
	protected HashMap<String, String> values;
	
	public SSDPMessage(String msg)
	{
		values = new HashMap<String, String>();
		String[] lines = msg.split("\r\n");
		for(int i = 1; i < lines.length; i++)
		{
			String line = lines[i];
			int pos = line.indexOf(":");
			if(pos > -1)
			{
				String key = line.substring(0, pos).trim().toUpperCase();
				String value = line.substring(pos + 1).trim();
				values.put(key, value);
			}
		}
	}
	
	public String getValue(String key)
	{
		return values.get(key.toUpperCase());
	}
	
	public void putValue(String key, String value)
	{
		values.put(key, value);
	}
	
	public String toString()
	{
		String ret = "";
		Iterator<String> it = values.keySet().iterator();
		while(it.hasNext())
		{
			String key = it.next();
			String value = values.get(key);
			ret = ret + key + ": " + value + "\r\n";
		}
		return ret + "\r\n";
	}
}
