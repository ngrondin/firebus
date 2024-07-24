package io.firebus.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
	
	public String toString()
	{
		return toString(false, false);
	}
	
	public String toString(boolean flat) {
		return toString(flat, false);
	}
	
	public String toString(boolean flat, boolean safe)
	{
		StringBuilder sb = new StringBuilder();
		String indentStr = flat == false ? "" : null;
		writeToStringBuilder(sb, indentStr, safe);
		return sb.toString();
	}
	
	@Deprecated
	public String toString(int indent, boolean flat)
	{
		return toString(flat);
	}
	
	protected abstract void writeToStringBuilder(StringBuilder sb, String indentStr, boolean safe);
	
	public abstract DataEntity getCopy();
}
