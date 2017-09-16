package com.nic.firebus.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


public class JSONList extends JSONEntity
{
	protected  ArrayList<JSONEntity> list;
	
	public JSONList()
	{
		list = new ArrayList<JSONEntity>();
	}
	
	public JSONList(InputStream is)
	{
		list = new ArrayList<JSONEntity>();
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
			
			if(c == '[')
			{
				list.add(readJSONValue(is));
				
				while((cInt = is.read()) != -1)
				{
					c = (char)cInt;
					if(c == ',')
					{
						list.add(readJSONValue(is));
					}
					else if(c == ']')
					{
						break;
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
	
	public int size()
	{
		return list.size();
	}
	
	public void add(JSONEntity o)
	{
		list.add(o);
	}

	
	public JSONEntity get(String key)
	{
		JSONEntity ret = null;
		int dot = key.indexOf('.');
		if(dot == -1)
		{
			ret = list.get(Integer.parseInt(key));
		}
		else
		{
			String root = key.substring(0, dot);
			String rest = key.substring(dot + 1);
			JSONEntity obj = list.get(Integer.parseInt(root));
			if(obj instanceof JSONObject)
				ret = ((JSONObject)obj).get(rest);
			else if(obj instanceof JSONList)
				ret = ((JSONList)obj).get(rest);
		}
		return ret;
	}

	public JSONEntity get(int i)
	{
		return list.get(i);
	}

	public String getString(int i)
	{
		JSONEntity obj = list.get(i);
		if(obj != null  &&  obj instanceof JSONLiteral)
			return ((JSONLiteral)obj).getString();
		else
			return null;		
	}
	
	public JSONObject getObject(int i)
	{
		JSONEntity obj = list.get(i);
		if(obj != null  &&  obj instanceof JSONObject)
			return (JSONObject)obj;
		else
			return null;		
	}
	
	public JSONList getList(int i)
	{
		JSONEntity obj = list.get(i);
		if(obj != null  &&  obj instanceof JSONList)
			return (JSONList)obj;
		else
			return null;		
	}
	
	public String toString()
	{
		return toString(0);
	}

	public String toString(int indent)
	{
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		sb.append('\r');
		for(int i = 0; i < list.size(); i++)
		{
			sb.append(indentString(indent + 1));
			sb.append(list.get(i).toString(indent + 1));
			if(i < list.size() - 1)
				sb.append(',');
			sb.append('\r');
		}
		sb.append(indentString(indent));
		sb.append(']');
		return sb.toString();
	}
	
	public JSONEntity getCopy()
	{
		JSONList ret = new JSONList();
		for(int i = 0; i < size(); i++)
			ret.add(get(i).getCopy());
		return ret;
	}
}
