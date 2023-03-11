package io.firebus.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class DataMap extends DataEntity implements Map<String, Object>
{
	protected HashMap<String, DataEntity> attributes = new HashMap<String, DataEntity>();
	
	public DataMap()
	{
	}

	public DataMap(String key, Object value)
	{
		put(key, value);
	}
	
	public DataMap(Object... o)
	{
		for(int i = 0; i < o.length; i+=2) {
			if(o[i] instanceof String) {
				String key = (String)o[i];
				Object value = o.length > i + 1 ? o[i+1] : null;
				put(key, value);				
			}
		}
	}
	
	public DataMap(String s) throws DataException
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

	public DataMap(InputStream is) throws IOException, DataException
	{
		initialise(is);
	}
	
	protected void initialise(InputStream is) throws IOException, DataException
	{
		boolean inString = false;
		boolean correctlyClosed = false;
		String key = "";
		DataEntity value = null;				
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
						throw new DataException("Expected '{' at line " + bis.getLine() + " column " + bis.getColumn());
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
					throw new DataException("Expected a new key at line " + bis.getLine() + " column " + bis.getColumn());
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
							throw new DataException("Illegal character at line " + bis.getLine() + " column " + bis.getColumn());
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
					throw new DataException("Expected ':' at line " + bis.getLine() + " column " + bis.getColumn());
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
					throw new DataException("Expected '}' at line " + bis.getLine() + " column " + bis.getColumn());
				}
			}
			bis.mark(1);
		}
		if(!correctlyClosed)
			throw new DataException("Missing '}' as line " + bis.getLine() + " column " + bis.getColumn());
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
	
	public Object put(String key, Object value)
	{
		DataEntity val = null;
		if(value instanceof DataEntity)
			val = (DataEntity)value;
		else
			val = new DataLiteral(value);
		
		int dot = key.indexOf('.');
		if(dot == -1)
		{
			attributes.put(key, val);
		}
		else
		{
			String root = key.substring(0, dot);
			String rest = key.substring(dot + 1);
			DataEntity obj = attributes.get(root);
			if(obj != null)
			{
				if(obj instanceof DataMap)
					((DataMap)obj).put(rest,  value);
			}
			else
			{
				attributes.put(key, val);
			}
		}
		return val;
	}
	
	@SuppressWarnings("rawtypes")
	public void putAll(Map m) 
	{
		
	}

	public void merge(DataMap other)
	{
		Iterator<String> it = other.keySet().iterator();
		while(it.hasNext())
		{
			String key = it.next();
			if(get(key) != null)
			{
				if(other.get(key) instanceof DataMap)
				{
					if(get(key) instanceof DataMap)
						getObject(key).merge(other.getObject(key));
					else
						put(key, other.get(key));
				}
				else if(other.get(key) instanceof DataList)
				{
					if(get(key) instanceof DataList)
						getList(key).merge(other.getList(key));
					else
						put(key, other.get(key));
				}
				else
				{
					put(key, other.get(key));
				}				
			}
			else
			{
				put(key, other.get(key));
			}
		}
	}
	
	public boolean matches(DataMap filter)
	{
		boolean matches = true;
		Iterator<String> it = filter.keySet().iterator();
		while(it.hasNext())
		{
			String key = it.next();
			if(!filter.get(key).equals(get(key)))
				matches = false;
		}
		return matches;
	}
	
	public Object remove(Object key)
	{
		if(key instanceof String)
		{
			String keyStr = (String)key;
			int dot = keyStr.indexOf('.');
			if(dot == -1)
			{
				return attributes.remove(key);
			}
			else
			{
				String root = keyStr.substring(0, dot);
				String rest = keyStr.substring(dot + 1);
				DataEntity obj = attributes.get(root);
				if(obj != null)
				{
					if(obj instanceof DataMap)
						return ((DataMap)obj).remove(rest);
					else
						return null;
				}
				else
				{
					return attributes.remove("key");
				}
			}				
		}
		else
		{
			return null;
		}
	}
	
	public DataEntity get(Object key)
	{
		DataEntity ret = null;
		if(key instanceof String)
		{
			String keyStr = (String)key;
			int dot = keyStr.indexOf('.');
			if(dot == -1)
			{
				ret = attributes.get(key);
			}
			else
			{
				String root = keyStr.substring(0, dot);
				String rest = keyStr.substring(dot + 1);
				DataEntity obj = attributes.get(root);
				if(obj != null)
				{
					if(obj instanceof DataMap)
						ret = ((DataMap)obj).get(rest);
					else if(obj instanceof DataList)
						ret = ((DataList)obj).get(rest);
				}
				else
				{
					ret = attributes.get(key);
				}
			}
			return ret;
		}
		else
		{
			return null;
		}
	}
	
	public String getString(String key)
	{
		DataEntity obj = get(key);
		if(obj != null  &&  obj instanceof DataLiteral)
			return ((DataLiteral)obj).getString();
		else
			return null;
	}
	
	public Number getNumber(String key)
	{
		DataEntity obj = get(key);
		if(obj != null  &&  obj instanceof DataLiteral)
			return ((DataLiteral)obj).getNumber();
		else
			return null;
	}

	public boolean getBoolean(String key)
	{
		DataEntity obj = get(key);
		if(obj != null  &&  obj instanceof DataLiteral)
			return ((DataLiteral)obj).getBoolean();
		else
			return false;
	}
	
	public Date getDate(String key)
	{
		DataEntity obj = get(key);
		if(obj != null  &&  obj instanceof DataLiteral)
			return ((DataLiteral)obj).getDate();
		else
			return null;
	}
		
	public DataMap getObject(String key)
	{
		DataEntity obj = get(key);
		if(obj != null  &&  obj instanceof DataMap)
			return (DataMap)obj;
		else
			return null;
	}
	
	public DataList getList(String key)
	{
		DataEntity obj = get(key);
		if(obj != null  &&  obj instanceof DataList)
			return (DataList)obj;
		else
			return null;
	}
	
	public boolean containsKey(Object key)
	{
		return attributes.containsKey(key);
	}
	
	public boolean containsValue(Object value) {
		return attributes.containsValue(value);
	}

	
	public Set<String> keySet()
	{
		return attributes.keySet();
	}
	

	public DataEntity getCopy()
	{
		DataMap ret = new DataMap();
		Iterator<String> it = keySet().iterator();
		while(it.hasNext())
		{
			String key = it.next();
			ret.put(key, get(key).getCopy());
		}
		return ret;
	}

	public int size() 
	{
		return attributes.size();
	}

	public boolean isEmpty() 
	{
		return attributes.isEmpty();
	}

	public void clear() 
	{
		attributes.clear();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection values() 
	{
		return attributes.values();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Set entrySet() 
	{
		return attributes.entrySet();
	}
	
	public String toString()
	{
		return toString(0, false);
	}

	public String toString(int indent, boolean flat)
	{
		StringBuilder sb = new StringBuilder();
		String indentStr = "";
		sb.append('{');
		if(!flat) {
			sb.append("\r\n");
			indentStr = indentString(indent + 1);
		}
		Iterator<Entry<String, DataEntity>> it = attributes.entrySet().iterator();
		while(it.hasNext())
		{
			Entry<String, DataEntity> entry = it.next();
			if(!flat)
				sb.append(indentStr);
			sb.append('"');
			sb.append(entry.getKey());
			sb.append('"');
			sb.append(':');
			sb.append(entry.getValue().toString(indent + 1, flat));
			if(it.hasNext())
				sb.append(',');
			if(!flat)
				sb.append("\r\n");
		}
		if(!flat)
			sb.append(indentString(indent));
		sb.append('}');
		return sb.toString();
	}
		
}
