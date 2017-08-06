package com.nic.firebus;

public abstract class FunctionInformation
{
	protected String functionName;

	public FunctionInformation(String n)
	{
		functionName = n;
	}
	
	public String getName()
	{
		return functionName;
	}
	
	
	public String toString()
	{
		return functionName;
	}
}
