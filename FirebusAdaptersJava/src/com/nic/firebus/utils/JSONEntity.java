package com.nic.firebus.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class JSONEntity
{
	
	protected JSONEntity readJSONValue(InputStream is) throws IOException
	{
		boolean inString = false;
		String valueString = "";
		JSONEntity value = null;				
		int cInt = -1;
		
		BufferedInputStream bis = null;
		if(is instanceof BufferedInputStream)
			bis = (BufferedInputStream)is;
		else
			bis = new BufferedInputStream(is);

		bis.mark(1);
		while((cInt = bis.read()) != -1)
		{
			char c = (char)cInt;
			if(c == '\"')
			{
				if(inString)
					inString = false;
				else
					inString = true;
			}
			else if((c == ',' || c == ']' || c == '}')  && !inString)
			{
				bis.reset();
				if(value == null)
					value = new JSONLiteral(valueString.trim());
				break;
			}
			else
			{
				if(!inString)
				{
					if(c == '{')
					{
						bis.reset();
						value = new JSONObject(bis);
						break;
					}
					else if(c == '[')
					{
						bis.reset();
						value = new JSONList(bis);
						break;
					}
				}
				valueString += c;
			}
			bis.mark(1);
		}
				
		return value;
	}
		
	
	public abstract String toString();
	
	public abstract String toString(int indent);
	
	protected String indentString(int indent)
	{
		String ret = "";
		for(int i = 0; i < indent; i++)
			ret = ret + "\t";
		return ret;
	}

	public abstract JSONEntity getCopy();
}
