package io.firebus.exceptions;

public class FunctionErrorException extends Exception
{
	private static final long serialVersionUID = 1L;
	protected int errorCode;

	public FunctionErrorException(String m)
	{
		super(m);
	}
	
	public FunctionErrorException(String m, int c)
	{
		super(m);
		errorCode = c;
	}
	
	public FunctionErrorException(String m, Throwable t)
	{
		super(m, t);
	}
	
	public FunctionErrorException(String m, Throwable t, int c) 
	{
		super(m, t);
		errorCode = c;
	}
	
	public int getErrorCode() 
	{
		return errorCode;
	}
}
