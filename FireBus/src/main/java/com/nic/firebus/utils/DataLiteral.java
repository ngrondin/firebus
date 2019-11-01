package com.nic.firebus.utils;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
//import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataLiteral extends DataEntity
{
	protected String stringValue;
	protected boolean boolValue;
	protected Number numberValue;
	protected Date dateValue;
	protected int valueType;
	protected static Pattern datePattern = Pattern.compile("^(?:[1-9]\\d{3}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1\\d|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[1-9]\\d(?:0[48]|[2468][048]|[13579][26])|(?:[2468][048]|[13579][26])00)-02-29)T(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d(?:\\.\\d{1,9})?(?:Z|[+-][01]\\d:[0-5]\\d)$");
	protected static Pattern numberPattern = Pattern.compile("[-+]?\\d*\\.?\\d+");
	protected static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); 
	//protected static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	
	static public int TYPE_NULL = 0;
	static public int TYPE_STRING = 1;
	static public int TYPE_NUMBER = 2;
	static public int TYPE_BOOLEAN = 3;
	static public int TYPE_DATE = 4;
	
	public DataLiteral(Object v)
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
		else if(v instanceof Date)
		{
			valueType = TYPE_DATE;
			dateValue = (Date)v;
		}
	}
	
	public DataLiteral(InputStream is) throws DataException, IOException
	{
		StringBuilder sb = null;
		boolean inString = false;
		int cInt = -1;
		char c = ' ';
		char previousC = ' ';
		int readState = 0; 
		boolean hadQuotes = false;

		PositionTrackingInputStream bis = null;
		if(is instanceof PositionTrackingInputStream)
			bis = (PositionTrackingInputStream)is;
		else
			bis = new PositionTrackingInputStream(is);
		
		bis.mark(1);
		while((cInt = bis.read()) != -1)
		{
			previousC = c;
			c = (char)cInt;
			if(readState == 0) // Before value
			{
				if(c != ' '  &&  c != '\r' && c != '\n' && c != '\t')
				{
					bis.reset();
					sb = new StringBuilder();
					readState = 1;
				}					
			}
			else if(readState == 1) // In value
			{
				if(inString)
				{
					if(c == '"'  &&  previousC != '\\')
					{
						inString = false;
						hadQuotes = true;
						String tempString = sb.toString();
						Matcher matcher = datePattern.matcher(tempString);
						if(matcher.matches())		
						{
							try
							{
								//dateValue = dateFormatter.parse(stringValue);
								valueType = TYPE_DATE;
								dateValue = dateFormat.parse(tempString);
							} 
							catch (ParseException e)
							{
								throw new DataException("Error processing date value");
							}
						}
						else
						{
							valueType = TYPE_STRING;
							stringValue = tempString;
						}
						break;
					}
					else
					{
						sb.append(c);
					}
				}
				else
				{
					if(c == '"')
					{
						if(sb.length() == 0)
							inString = true;
						else
							throw new DataException("Illegal character at line " + bis.getLine() + " column " + bis.getColumn());
					}
					else if(c == ' ' || c == '\r' || c == '\n' || c == '\t' || c == ',' || c == '}' || c == ']')
					{
						String tempString = sb.toString();
						bis.reset();
						if(!hadQuotes)
						{
							Matcher numberMatcher = numberPattern.matcher(tempString);
							if(tempString.equalsIgnoreCase("true")  ||  tempString.equalsIgnoreCase("false"))
							{
								valueType = TYPE_BOOLEAN;
								boolValue = tempString.equalsIgnoreCase("true") ? true : false;
							}
							else if(tempString.equalsIgnoreCase("null"))
							{
								valueType = TYPE_NULL;
							}
							else if(numberMatcher.matches())
							{
								valueType = TYPE_NUMBER;
								numberValue = Double.parseDouble(tempString);
							}
							else
							{
								valueType = TYPE_STRING;
								stringValue = tempString;
							}
						}
						else
						{
							valueType = TYPE_STRING;
							stringValue = tempString;
						}
						break;
					}
					else
					{
						sb.append(c);
					}
				}
			}
			bis.mark(1);
		}
	}
	
	public String getString()
	{
		if(valueType == TYPE_NULL)
			return null;
		if(valueType == TYPE_STRING)
			return stringValue;
		else if(valueType == TYPE_NUMBER)
			return "" + numberValue;
		else if(valueType == TYPE_BOOLEAN)
			return "" + boolValue;
		else if(valueType == TYPE_DATE)
			return (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")).format(dateValue);
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
			try
			{
				return Double.parseDouble(stringValue);
			}
			catch(NumberFormatException e)
			{
				return 0;
			}
		else if(valueType == TYPE_NUMBER)
			return numberValue;
		else if(valueType == TYPE_BOOLEAN)
			return boolValue == true ? 1 : 0;
		else if(valueType == TYPE_DATE)
			return dateValue.getTime();
		return 0;
	}
	
	public Date getDate()
	{
		if(valueType == TYPE_STRING)
			return null;
		else if(valueType == TYPE_NUMBER)
			return new Date(numberValue.longValue());
		else if(valueType == TYPE_BOOLEAN)
			return null;
		else if(valueType == TYPE_DATE)
			return dateValue;
		return null;
	}		
	
	public Object getObject()
	{
		if(valueType == TYPE_NULL)
			return null;
		if(valueType == TYPE_STRING)
			return stringValue;
		else if(valueType == TYPE_NUMBER)
			return numberValue;
		else if(valueType == TYPE_BOOLEAN)
			return boolValue;
		else if(valueType == TYPE_DATE)
			return dateValue;
		return null;
	}
	
	public int getType()
	{
		return valueType;
	}
	
	public boolean equals(Object o)
	{
		Object otherObject = o;
		if(o instanceof DataLiteral)
			otherObject = ((DataLiteral)o).getObject();
		return getObject().equals(otherObject);
	}
	
	public String toString()
	{
		return toString(0);
	}

	public String toString(int indent)
	{
		if(valueType == TYPE_NULL)
			return "null";
		else if(valueType == TYPE_STRING  ||  valueType == TYPE_DATE)
			return "\"" + getString().replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\"";
		else
			return getString();
	}
	
	public DataLiteral getCopy()
	{
		if(valueType == TYPE_NULL)
			return new DataLiteral((Object)null);
		if(valueType == TYPE_STRING)
			return new DataLiteral(new String(stringValue));
		else if(valueType == TYPE_NUMBER)
			return new DataLiteral(new Double((Double)numberValue));
		else if(valueType == TYPE_BOOLEAN)
			return new DataLiteral(new Boolean(boolValue));
		else if(valueType == TYPE_DATE)
			return new DataLiteral(new Date(dateValue.getTime()));
		return null;
	}
}
