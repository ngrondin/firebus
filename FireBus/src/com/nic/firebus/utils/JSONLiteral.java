package com.nic.firebus.utils;

import java.io.IOException;
import java.io.InputStream;

public class JSONLiteral extends JSONEntity
{
	protected String stringValue;
	protected boolean boolValue;
	protected Number numberValue;
	protected int valueType;
	
	static public int TYPE_NULL = 0;
	static public int TYPE_STRING = 1;
	static public int TYPE_NUMBER = 2;
	static public int TYPE_BOOLEAN = 3;
	
	public JSONLiteral(Object v)
	{
		if(v == null)
		{
			valueType = TYPE_NULL;
		}
		else if(v instanceof String)
		{
			valueType = TYPE_STRING;
			stringValue = (String)v;
		}
		else if(v instanceof Number)
		{
			valueType = TYPE_NUMBER;
			numberValue = (Number)v;
		}
		else if(v instanceof Boolean)
		{
			valueType = TYPE_BOOLEAN;
			boolValue = (Boolean)v;
		}
	}
	
	public JSONLiteral(InputStream is) throws JSONException, IOException
	{
		boolean inString = false;
		int cInt = -1;
		char c = ' ';
		int readState = 0; 
		boolean hadQuotes = false;
		valueType = TYPE_STRING;

		PositionTrackingInputStream bis = null;
		if(is instanceof PositionTrackingInputStream)
			bis = (PositionTrackingInputStream)is;
		else
			bis = new PositionTrackingInputStream(is);
		
		bis.mark(1);
		while((cInt = bis.read()) != -1)
		{
			c = (char)cInt;
			if(readState == 0) // Before value
			{
				if(c != ' '  &&  c != '\r' && c != '\n' && c != '\t')
				{
					bis.reset();
					stringValue = "";
					readState = 1;
				}					
			}
			else if(readState == 1) // In value
			{
				if(inString)
				{
					if(c == '"')
					{
						inString = false;
						hadQuotes = true;
						break;
					}
					else
					{
						stringValue += c;
					}
				}
				else
				{
					if(c == '"')
					{
						if(stringValue.equals(""))
							inString = true;
						else
							throw new JSONException("Illegal character at line " + bis.getLine() + " column " + bis.getColumn());
					}
					else if(c == ' ' || c == '\r' || c == '\n' || c == '\t' || c == ',' || c == '}' || c == ']')
					{
						bis.reset();
						if(!hadQuotes)
						{
							if(stringValue.equalsIgnoreCase("true")  ||  stringValue.equalsIgnoreCase("false"))
							{
								valueType = TYPE_BOOLEAN;
								boolValue = stringValue.equalsIgnoreCase("true") ? true : false;
							}
							else if(stringValue.matches("[-+]?\\d*\\.?\\d+"))
							{
								valueType = TYPE_NUMBER;
								numberValue = Double.parseDouble(stringValue);
							}
						}
						break;
					}
					else
					{
						stringValue += c;
					}
				}
			}
			bis.mark(1);
		}
	}
	
	public String getString()
	{
		if(valueType == TYPE_NULL)
			return "";
		if(valueType == TYPE_STRING)
			return stringValue;
		else if(valueType == TYPE_NUMBER)
			return "" + numberValue;
		else if(valueType == TYPE_BOOLEAN)
			return "" + boolValue;
		return "";
	}

	public boolean getBoolean()
	{
		if(valueType == TYPE_BOOLEAN)
			return boolValue;
		return false;
	}
	
	public Number getNumber()
	{
		if(valueType == TYPE_STRING)
			return 0;
		else if(valueType == TYPE_NUMBER)
			return numberValue;
		else if(valueType == TYPE_BOOLEAN)
			return boolValue == true ? 1 : 0;
		return 0;
	}
	
	public int getType()
	{
		return valueType;
	}
	
	public String toString()
	{
		return toString(0);
	}

	public String toString(int indent)
	{
		if(valueType == TYPE_NULL)
			return "null";
		if(valueType == TYPE_STRING)
			return "\"" + getString() + "\"";
		else
			return getString();
	}
	
	public JSONLiteral getCopy()
	{
		if(valueType == TYPE_NULL)
			return new JSONLiteral((Object)null);
		if(valueType == TYPE_STRING)
			return new JSONLiteral(new String(stringValue));
		else if(valueType == TYPE_NUMBER)
			return new JSONLiteral(new Double((Double)numberValue));
		else if(valueType == TYPE_BOOLEAN)
			return new JSONLiteral(new Boolean(boolValue));
		return null;
	}
}
