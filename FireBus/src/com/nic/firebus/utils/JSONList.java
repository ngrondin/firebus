package com.nic.firebus.utils;

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
	
	public JSONList(InputStream is) throws JSONException, IOException
	{
		list = new ArrayList<JSONEntity>();
		int cInt = -1;
		boolean correctlyClosed = false;
		char c = ' ';
		int readState = 0; 

		PositionTrackingInputStream bis = null;
		if(is instanceof PositionTrackingInputStream)
			bis = (PositionTrackingInputStream)is;
		else
			bis = new PositionTrackingInputStream(is);
		
		bis.mark(1);
		while((cInt = bis.read()) != -1)
		{
			c = (char)cInt;
			if(readState == 0) // Before opening bracket
			{
				if(c != ' '  &&  c != '\r' && c != '\n' && c != '\t')
				{
					if(c == '[')
					{
						readState = 1;
					}
					else
						throw new JSONException("Expected '[' at line " + bis.getLine() + " column " + bis.getColumn());
				}						
			}
			else if(readState == 1) // before first value
			{
				if(c == ']')
				{
					correctlyClosed = true;
					break;
				}
				else if(c == ',')
				{
					throw new JSONException("Expected value or ']' at line " + bis.getLine() + " column " + bis.getColumn());
				}
				else if(c != ' '  &&  c != '\r' && c != '\n' && c != '\t')
				{
					bis.reset();
					JSONEntity value = readJSONValue(bis);
					if(value != null)
						list.add(value);
					readState = 3;
				}
			}
			else if(readState == 2) // before subsequent values
			{
				if(c == ']'  ||  c == ',')
				{
					throw new JSONException("Expected value at line " + bis.getLine() + " column " + bis.getColumn());
				}
				else if(c != ' '  &&  c != '\r' && c != '\n' && c != '\t')
				{
					bis.reset();
					JSONEntity value = readJSONValue(bis);
					if(value != null)
						list.add(value);
					readState = 3;
				}
			}
			else if(readState == 3)  //  after value
			{
				if(c == ']')
				{
					correctlyClosed = true;
					break;
				}
				else if(c == ',')
				{
					readState = 2;
				}
				else if(c != ' '  &&  c != '\r' && c != '\n' && c != '\t')
					throw new JSONException("Expected ',' or ']' at line " + bis.getLine() + " column " + bis.getColumn());				
			}
			bis.mark(1);
		}
		if(!correctlyClosed)
			throw new JSONException("Missing ']' as line " + bis.getLine() + " column " + bis.getColumn());

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
	
	public Number getNumber(int i)
	{
		JSONEntity obj = list.get(i);
		if(obj != null  &&  obj instanceof JSONLiteral)
			return ((JSONLiteral)obj).getNumber();
		else
			return null;
	}

	public boolean getBoolean(int i)
	{
		JSONEntity obj = list.get(i);
		if(obj != null  &&  obj instanceof JSONLiteral)
			return ((JSONLiteral)obj).getBoolean();
		else
			return false;
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
		sb.append("\r\n");
		for(int i = 0; i < list.size(); i++)
		{
			sb.append(indentString(indent + 1));
			sb.append(list.get(i).toString(indent + 1));
			if(i < list.size() - 1)
				sb.append(',');
			sb.append("\r\n");
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
