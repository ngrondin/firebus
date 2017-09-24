package com.nic.firebus;

import com.nic.firebus.interfaces.BusFunction;

public class FunctionEntry
{
	protected String serviceName;
	protected BusFunction function;
	protected int maxConcurrent;
	protected int currentCount;
	
	public FunctionEntry(String sn, BusFunction f, int mc)
	{
		serviceName = sn;
		function = f;
		maxConcurrent = mc;
		currentCount = 0;
	}
	
	public void setFunction(BusFunction f)
	{
		function = f;
	}
	
	public void runStarted()
	{
		currentCount++;
	}
	
	public void runEnded()
	{
		currentCount--;
	}
	
	public boolean canRunOneMore()
	{
		return currentCount < maxConcurrent;
	}
}
