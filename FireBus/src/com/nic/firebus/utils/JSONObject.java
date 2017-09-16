package com.nic.firebus.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;




public class JSONObject extends JSONEntity
{
	protected HashMap<String, JSONEntity> attributes;
	
	public JSONObject()
	{
		attributes = new HashMap<String, JSONEntity>();
	}

	public JSONObject(String s)
	{
		initialise(new ByteArrayInputStream(s.getBytes()));
	}

	public JSONObject(InputStream is)
	{
		initialise(is);
	}
	
	public void initialise(InputStream is)
	{
		attributes = new HashMap<String, JSONEntity>();
		boolean inString = false;
		boolean inValueName = true;
		String valueName = "";
		JSONEntity value = null;				
		int cInt = -1;
		char c = ' ';
		
		BufferedInputStream bis = null;
		if(is instanceof BufferedInputStream)
			bis = (BufferedInputStream)is;
		else
			bis = new BufferedInputStream(is);
		
		try
		{
			while(c == ' '  && c != -1)
				c = (char)bis.read();
			
			if(c == '{')
			{
				while((cInt = bis.read()) != -1)
				{
					c = (char)cInt;
					if(c == '\"')
					{
						if(inString)
							inString = false;
						else
							inString = true;
					}
					else if(c == ':'  &&   !inString)
					{
						value = readJSONValue(bis);
						inValueName = false;
					}
					else if((c == ',' || c == '}')  && !inString)
					{
						attributes.put(valueName.trim(), value);
						valueName = "";
						value = null;
						inValueName = true;
						if(c == '}')
							break;
					}			
					else if(inValueName)
					{
						valueName += c;
					}
				}
			}					
		}
		catch(IOException e)
		{
	
		}
	}
	
	public void write(OutputStream os)
	{
		try
		{
			String str = toString();
			os.write(str.getBytes());
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}		
	}
	
	public void put(String key, JSONEntity value)
	{
		int dot = key.indexOf('.');
		if(dot == -1)
		{
			attributes.put(key, value);
		}
		else
		{
			String root = key.substring(0, dot);
			String rest = key.substring(dot + 1);
			JSONEntity obj = attributes.get(root);
			if(obj instanceof JSONObject)
				((JSONObject)obj).put(rest,  value);
		}		
	}
	
	public void put(String key, String value)
	{
		JSONEntity val = new JSONLiteral(value);
		put(key, val);
	}
	
	public JSONEntity get(String key)
	{
		JSONEntity ret = null;
		int dot = key.indexOf('.');
		if(dot == -1)
		{
			ret = attributes.get(key);
		}
		else
		{
			String root = key.substring(0, dot);
			String rest = key.substring(dot + 1);
			JSONEntity obj = attributes.get(root);
			if(obj instanceof JSONObject)
				ret = ((JSONObject)obj).get(rest);
			else if(obj instanceof JSONList)
				ret = ((JSONList)obj).get(rest);
		}
		return ret;
	}
	
	public String getString(String key)
	{
		JSONEntity obj = get(key);
		if(obj != null  &&  obj instanceof JSONLiteral)
			return ((JSONLiteral)obj).getString();
		else
			return null;
	}
	
	public JSONObject getObject(String key)
	{
		JSONEntity obj = get(key);
		if(obj != null  &&  obj instanceof JSONObject)
			return (JSONObject)obj;
		else
			return null;
	}
	
	public JSONList getList(String key)
	{
		JSONEntity obj = get(key);
		if(obj != null  &&  obj instanceof JSONList)
			return (JSONList)obj;
		else
			return null;
	}
	
	public Set<String> keySet()
	{
		return attributes.keySet();
	}
	
	public String toString()
	{
		return toString(0);
	}

	public String toString(int indent)
	{
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		sb.append('\r');
		Iterator<String> it = attributes.keySet().iterator();
		while(it.hasNext())
		{
			String valueName = it.next();
			sb.append(indentString(indent + 1));
			sb.append('"');
			sb.append(valueName);
			sb.append('"');
			sb.append(':');
			sb.append(attributes.get(valueName).toString(indent + 1));
			if(it.hasNext())
				sb.append(',');
			sb.append('\r');
		}
		sb.append(indentString(indent));
		sb.append('}');
		return sb.toString();
	}
	
	public JSONEntity getCopy()
	{
		JSONObject ret = new JSONObject();
		Iterator<String> it = keySet().iterator();
		while(it.hasNext())
		{
			String key = it.next();
			ret.put(key, get(key).getCopy());
		}
		return ret;
	}
}
