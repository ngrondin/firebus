package com.nic.firebus.utils;

import java.io.IOException;
import java.io.InputStream;

public class JSONLiteral extends JSONEntity
{
	protected String value;
	
	public JSONLiteral(String s)
	{
		value = s;
	}
	
	public JSONLiteral(InputStream is) throws JSONException, IOException
	{
		boolean inString = false;
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
			if(readState == 0) // Before value
			{
				if(c != ' '  &&  c != '\r' && c != '\n' && c != '\t')
				{
					bis.reset();
					value = "";
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
						break;
					}
					else
					{
						value += c;
					}
				}
				else
				{
					if(c == '"')
					{
						if(value.equals(""))
							inString = true;
						else
							throw new JSONException("Illegal character at line " + bis.getLine() + " column " + bis.getColumn());
					}
					else if(c == ' ' || c == '\r' || c == '\n' || c == '\t' || c == ',' || c == '}' || c == ']')
					{
						bis.reset();
						break;
					}
					else
					{
						value += c;
					}
				}
			}
			bis.mark(1);
		}
	}
	
	public String getString()
	{
		return value;
	}
	
	public String toString()
	{
		return toString(0);
	}

	public String toString(int indent)
	{
		if(value == null)
			return "\"\"";
		else
			return "\"" + getString() + "\"";
	}
	
	public JSONLiteral getCopy()
	{
		return new JSONLiteral(new String(value));
	}
}
