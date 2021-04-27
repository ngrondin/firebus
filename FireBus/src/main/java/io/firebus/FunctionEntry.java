package io.firebus;

import io.firebus.interfaces.BusFunction;

public class FunctionEntry
{
	protected String name;
	protected BusFunction function;
	protected int maxConcurrent;
	protected int currentCount;
	protected boolean[] reservedIds;
	
	public FunctionEntry(String n, BusFunction f, int mc)
	{
		name = n;
		function = f;
		maxConcurrent = mc;
		reservedIds = new boolean[maxConcurrent];
		currentCount = 0;
	}
	
	public void setFunction(BusFunction f)
	{
		function = f;
	}
	
	public synchronized long getExecutionId()
	{
		if(currentCount < maxConcurrent) {
			for(int i = 0; i < maxConcurrent; i++) {
				if(reservedIds[i] == false) {
					reservedIds[i] = true;
					currentCount++;
					return (long)i;
				}
			}
		}
		return -1;
	}
	
	public synchronized void releaseExecutionId(long id)
	{
		int i = (int)id;
		if(reservedIds[i] == true) {
			reservedIds[i] = false;
			currentCount--;
		}
	}
	
	public String getName() 
	{
		return name;
	}
}
