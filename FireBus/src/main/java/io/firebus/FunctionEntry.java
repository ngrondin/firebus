package io.firebus;

import io.firebus.information.Statistics;
import io.firebus.interfaces.BusFunction;
import io.firebus.utils.DataMap;

public class FunctionEntry
{
	protected String name;
	protected BusFunction function;
	protected int limitConcurrent;
	protected boolean[] reservedIds;
	protected int currentCount;
	protected int maxCountSinceReset;
	protected int maxCountAllTime;
	
	public FunctionEntry(String n, BusFunction f, int mc)
	{
		name = n;
		function = f;
		limitConcurrent = mc;
		reservedIds = new boolean[limitConcurrent];
		currentCount = 0;
		maxCountSinceReset = 0;
		maxCountAllTime = 0;
	}
	
	public void setFunction(BusFunction f)
	{
		function = f;
	}
	
	public synchronized long getExecutionId()
	{
		if(currentCount < limitConcurrent) {
			for(int i = 0; i < limitConcurrent; i++) {
				if(reservedIds[i] == false) {
					reservedIds[i] = true;
					currentCount++;
					if(currentCount > maxCountSinceReset) {
						maxCountSinceReset = currentCount;
						if(maxCountSinceReset > maxCountAllTime)
							maxCountAllTime = maxCountSinceReset;
					}
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
	
	public int getExecutionCount()
	{
		return currentCount;
	}
	
	public Statistics getStatistics() 
	{
		Statistics stat = new Statistics(name, maxCountSinceReset, maxCountAllTime, limitConcurrent);
		maxCountSinceReset = 0;
		return stat;
	}
	
	public DataMap getStatus() {
		DataMap status = new DataMap();
		status.put("limit", limitConcurrent);
		status.put("current", currentCount);
		status.put("maxSinceLast", maxCountSinceReset);
		status.put("maxAllTime", maxCountAllTime);
		return status;
	}
}
