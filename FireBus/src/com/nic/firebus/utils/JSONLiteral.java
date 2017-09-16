package com.nic.firebus.utils;

public class JSONLiteral extends JSONEntity
{
	protected String value;
	
	public JSONLiteral(String s)
	{
		value = s;
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
		return "\"" + getString() + "\"";
	}
	
	public JSONLiteral getCopy()
	{
		return new JSONLiteral(new String(value));
	}
}
