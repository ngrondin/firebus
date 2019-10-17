package com.nic.firebus.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


public class DataList extends DataEntity
{
	protected  ArrayList<DataEntity> list;
	
	public DataList()
	{
		list = new ArrayList<DataEntity>();
	}
	
	public DataList(InputStream is) throws DataException, IOException
	{
		list = new ArrayList<DataEntity>();
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
						throw new DataException("Expected '[' at line " + bis.getLine() + " column " + bis.getColumn());
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
					throw new DataException("Expected value or ']' at line " + bis.getLine() + " column " + bis.getColumn());
				}
				else if(c != ' '  &&  c != '\r' && c != '\n' && c != '\t')
				{
					bis.reset();
					DataEntity value = readJSONValue(bis);
					if(value != null)
						list.add(value);
					readState = 3;
				}
			}
			else if(readState == 2) // before subsequent values
			{
				if(c == ']'  ||  c == ',')
				{
					throw new DataException("Expected value at line " + bis.getLine() + " column " + bis.getColumn());
				}
				else if(c != ' '  &&  c != '\r' && c != '\n' && c != '\t')
				{
					bis.reset();
					DataEntity value = readJSONValue(bis);
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
					throw new DataException("Expected ',' or ']' at line " + bis.getLine() + " column " + bis.getColumn());				
			}
			bis.mark(1);
		}
		if(!correctlyClosed)
			throw new DataException("Missing ']' as line " + bis.getLine() + " column " + bis.getColumn());

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
	
	public void add(Object o)
	{
		if(o instanceof DataEntity)
			list.add((DataEntity)o);
		else
			list.add(new DataLiteral(o));		
	}
	
	public void merge(DataList other)
	{
		for(int i = 0; i < other.size(); i++)
		{
			boolean exists = false;
			for(int j = 0; j < size(); j++)
				if(get(j).toString().equals(other.get(i).toString()))
					exists = true;
			if(!exists)
				add(other.get(i));
		}		
	}
	
	public void remove(int i)
	{
		list.remove(i);
	}
	
	public DataEntity get(String key)
	{
		DataEntity ret = null;
		int dot = key.indexOf('.');
		if(dot == -1)
		{
			ret = list.get(Integer.parseInt(key));
		}
		else
		{
			String root = key.substring(0, dot);
			String rest = key.substring(dot + 1);
			DataEntity obj = list.get(Integer.parseInt(root));
			if(obj instanceof DataMap)
				ret = ((DataMap)obj).get(rest);
			else if(obj instanceof DataList)
				ret = ((DataList)obj).get(rest);
		}
		return ret;
	}

	public DataEntity get(int i)
	{
		return list.get(i);
	}

	public String getString(int i)
	{
		DataEntity obj = list.get(i);
		if(obj != null  &&  obj instanceof DataLiteral)
			return ((DataLiteral)obj).getString();
		else
			return null;		
	}
	
	public Number getNumber(int i)
	{
		DataEntity obj = list.get(i);
		if(obj != null  &&  obj instanceof DataLiteral)
			return ((DataLiteral)obj).getNumber();
		else
			return null;
	}

	public boolean getBoolean(int i)
	{
		DataEntity obj = list.get(i);
		if(obj != null  &&  obj instanceof DataLiteral)
			return ((DataLiteral)obj).getBoolean();
		else
			return false;
	}
	
	public DataMap getObject(int i)
	{
		DataEntity obj = list.get(i);
		if(obj != null  &&  obj instanceof DataMap)
			return (DataMap)obj;
		else
			return null;		
	}
	
	public DataList getList(int i)
	{
		DataEntity obj = list.get(i);
		if(obj != null  &&  obj instanceof DataList)
			return (DataList)obj;
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
	
	public DataEntity getCopy()
	{
		DataList ret = new DataList();
		for(int i = 0; i < size(); i++)
			ret.add(get(i).getCopy());
		return ret;
	}
}
