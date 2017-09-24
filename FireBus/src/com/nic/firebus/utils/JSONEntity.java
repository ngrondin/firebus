package com.nic.firebus.utils;

import java.io.IOException;
import java.io.InputStream;

public abstract class JSONEntity
{
	
	protected JSONEntity readJSONValue(InputStream is) throws IOException, JSONException
	{
		JSONEntity value = null;				
		int cInt = -1;
		
		PositionTrackingInputStream bis = null;
		if(is instanceof PositionTrackingInputStream)
			bis = (PositionTrackingInputStream)is;
		else
			bis = new PositionTrackingInputStream(is);

		bis.mark(1);
		while((cInt = bis.read()) != -1)
		{
			char c = (char)cInt;
			if(c != ' '  &&  c != '\r' && c != '\n' && c != '\t')
			{
				bis.reset();
				if(c == '{')
					value = new JSONObject(bis);
				else if(c == '[')
					value = new JSONList(bis);
				else
					value = new JSONLiteral(bis);
				break;
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
