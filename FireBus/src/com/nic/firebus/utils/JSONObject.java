package com.nic.firebus.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
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

	public JSONObject(String s) throws JSONException
	{
		try
		{
			initialise(new ByteArrayInputStream(s.getBytes()));
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	public JSONObject(InputStream is) throws IOException, JSONException
	{
		initialise(is);
	}
	
	protected void initialise(InputStream is) throws IOException, JSONException
	{
		attributes = new HashMap<String, JSONEntity>();
		boolean inString = false;
		boolean correctlyClosed = false;
		String key = "";
		JSONEntity value = null;				
		int cInt = -1;
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
					if(c == '{')
						readState = 1;
					else
						throw new JSONException("Expected '{' at line " + bis.getLine() + " column " + bis.getColumn());
				}				
			}
			else if(readState == 1) // Before key
			{
				if(c == '{' || c == '}' || c == '[' || c == ']' || c == ',')
				{
					if(c == '}'  &&  attributes.isEmpty())
					{
						correctlyClosed = true;
						break;
					}
					throw new JSONException("Expected a new key at line " + bis.getLine() + " column " + bis.getColumn());
				}
				if(c != ' '  &&  c != '\r' && c != '\n' && c != '\t')
				{
					key = "";
					bis.reset();
					readState = 2;
				}					
			}
			else if(readState == 2) // In key
			{
				if(inString)
				{
					if(c == '"')
					{
						inString = false;
						readState = 3;
					}
					else
					{
						key += c;
					}
				}
				else
				{
					if(c == ' ' || c == '\r' || c == '\n' || c == '\t')
					{
						readState = 3;
					}
					else if(c == ':')
					{
						readState = 4;
					}
					else if(c == '"')
					{
						if(key.equals(""))
							inString = true;
						else
							throw new JSONException("Illegal character at line " + bis.getLine() + " column " + bis.getColumn());
					}
					else
					{
						key += c;
					}
				}
			}
			else if(readState == 3) // After Key
			{
				if(c == ':')
				{
					readState = 4;
				}
				else if(c != ' '  &&  c != '\r' && c != '\n' && c != '\t')
				{
					throw new JSONException("Expected ':' at line " + bis.getLine() + " column " + bis.getColumn());
				}
			}
			else if(readState == 4) // before value
			{
				bis.reset();
				value = readJSONValue(bis);
				attributes.put(key, value);
				readState = 5;
			}
			else if(readState == 5) // After value
			{
				if(c == '}')
				{
					correctlyClosed = true;
					break;
				}
				else if(c == ',')
				{
					readState = 1;
				}
				else if(c != ' ' &&  c != '\r' && c != '\n' && c != '\t')
				{
					throw new JSONException("Expected '}' at line " + bis.getLine() + " column " + bis.getColumn());
				}
			}
			bis.mark(1);
		}
		if(!correctlyClosed)
			throw new JSONException("Missing '}' as line " + bis.getLine() + " column " + bis.getColumn());
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
	
	public void put(String key, Object value)
	{
		int dot = key.indexOf('.');
		if(dot == -1)
		{
			if(value instanceof JSONEntity)
				attributes.put(key, (JSONEntity)value);
			else
				attributes.put(key, new JSONLiteral(value));
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
	
	public Number getNumber(String key)
	{
		JSONEntity obj = get(key);
		if(obj != null  &&  obj instanceof JSONLiteral)
			return ((JSONLiteral)obj).getNumber();
		else
			return null;
	}

	
	public boolean getBoolean(String key)
	{
		JSONEntity obj = get(key);
		if(obj != null  &&  obj instanceof JSONLiteral)
			return ((JSONLiteral)obj).getBoolean();
		else
			return false;
	}
	
	public Date getDate(String key)
	{
		JSONEntity obj = get(key);
		if(obj != null  &&  obj instanceof JSONLiteral)
			return ((JSONLiteral)obj).getDate();
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
	
	public boolean containsKey(String key)
	{
		return attributes.containsKey(key);
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
		sb.append("\r\n");
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
			sb.append("\r\n");
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
