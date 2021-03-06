package io.firebus.utils;

import java.io.IOException;
import java.io.InputStream;

public abstract class DataEntity
{
	
	protected DataEntity readJSONValue(InputStream is) throws IOException, DataException
	{
		DataEntity value = null;				
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
					value = new DataMap(bis);
				else if(c == '[')
					value = new DataList(bis);
				else
					value = new DataLiteral(bis);
				break;
			}
			bis.mark(1);
		}				
		return value;
	}
		
	
	public abstract String toString();
	
	public abstract String toString(int indent, boolean flat);
	
	protected String indentString(int indent)
	{
		String ret = "";
		for(int i = 0; i < indent; i++)
			ret = ret + "\t";
		return ret;
	}

	public abstract DataEntity getCopy();
}
