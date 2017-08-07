package com.nic.firebus.exceptions;

public class FunctionErrorException extends Exception
{
	private static final long serialVersionUID = 1L;

	public FunctionErrorException(String m)
	{
		super(m);
	}
}
