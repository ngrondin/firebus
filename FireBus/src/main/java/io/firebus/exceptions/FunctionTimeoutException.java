package io.firebus.exceptions;

public class FunctionTimeoutException extends Exception
{
	private static final long serialVersionUID = 1L;

	public FunctionTimeoutException(String m)
	{
		super(m);
	}
}
