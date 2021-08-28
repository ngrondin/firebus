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
	protected long[] start;
	protected int concurrentCount;
	protected int maxConcurrentCountSinceReset;
	protected int maxConcurrentCountAllTime;
	protected long entryStart;
	protected long cumulExecutionTime;
	protected long executionCount;
	
	public FunctionEntry(String n, BusFunction f, int mc)
	{
		name = n;
		function = f;
		limitConcurrent = mc;
		reservedIds = new boolean[limitConcurrent];
		start = new long[limitConcurrent];
		entryStart = System.currentTimeMillis();
		concurrentCount = 0;
		maxConcurrentCountSinceReset = 0;
		maxConcurrentCountAllTime = 0;
		cumulExecutionTime = 0;
		executionCount = 0;
	}
	
	public void setFunction(BusFunction f)
	{
		function = f;
	}
	
	public synchronized long getExecutionId()
	{
		if(concurrentCount < limitConcurrent) {
			for(int i = 0; i < limitConcurrent; i++) {
				if(reservedIds[i] == false) {
					reservedIds[i] = true;
					start[i] = System.currentTimeMillis();
					executionCount++;
					concurrentCount++;
					if(concurrentCount > maxConcurrentCountSinceReset) {
						maxConcurrentCountSinceReset = concurrentCount;
						if(maxConcurrentCountSinceReset > maxConcurrentCountAllTime)
							maxConcurrentCountAllTime = maxConcurrentCountSinceReset;
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
			concurrentCount--;
			cumulExecutionTime += (System.currentTimeMillis() - start[i]);
			start[i] = 0;
		}
	}
	
	public String getName() 
	{
		return name;
	}
	
	public int getExecutionCount()
	{
		return concurrentCount;
	}
	
	public Statistics getStatistics() 
	{
		Statistics stat = new Statistics(name, maxConcurrentCountSinceReset, maxConcurrentCountAllTime, limitConcurrent);
		maxConcurrentCountSinceReset = 0;
		return stat;
	}
	
	public DataMap getStatus() {
		DataMap status = new DataMap();
		status.put("concurrentLimit", limitConcurrent);
		status.put("currentExecutions", concurrentCount);
		status.put("maxConcurrentSinceLast", maxConcurrentCountSinceReset);
		status.put("maxConcurrentAllTime", maxConcurrentCountAllTime);
		status.put("totalExecutionCount", executionCount);
		status.put("cumulExecutionTime", cumulExecutionTime);
		status.put("utilisation", (100 * cumulExecutionTime / (System.currentTimeMillis() - entryStart)));
		return status;
	}
}
