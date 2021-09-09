package io.firebus.threads;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import io.firebus.NodeCore;
import io.firebus.data.DataMap;

public class FirebusThread extends Thread
{
	private Logger logger = Logger.getLogger("io.firebus");
	protected NodeCore nodeCore;
	protected ThreadManager threadManager;
	protected boolean quit;
	protected boolean ready;
	protected long threadStart;
	protected long expiry;
	protected String functionName;
	protected long functionExecutionId = -1;
	protected String trackingId;
	protected long lastStart;
	protected long lastCompletion;
	protected int totalExecutionCount;
	protected long cumulExecutionTime;
	protected long maxExecutionTime;
	protected String maxExecutionTrackingId;
	
	
	public FirebusThread(ThreadManager tm, NodeCore c)
	{
		threadManager = tm;
		nodeCore = c;
		quit = false;
		ready = false;
		expiry = -1;
		lastStart = -1;
		lastCompletion = -1;
		threadStart = System.currentTimeMillis();
	}
	
		
	public void run()
	{
		while(!quit)
		{
			try
			{
				FirebusRunnable fbRunnable = threadManager.getNext();
				if(fbRunnable != null)
				{
					functionName = fbRunnable.functionName;
					functionExecutionId = fbRunnable.functionExecutionId;
					lastStart = System.currentTimeMillis();
					lastCompletion = -1;
					expiry = fbRunnable.expiry;
					fbRunnable.runnable.run();
					completeExecution();
				}
				else
				{
					if(!quit) 
					{
						synchronized(this)
						{
							expiry = -1;
							ready = true;
							wait();
							ready = false;
						}
					}
				}
			} 
			catch (Exception e)
			{
				completeExecution();
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				logger.severe(sw.toString());
			}
		}
	}
	
	private void completeExecution()
	{
		totalExecutionCount++;
		lastCompletion = System.currentTimeMillis();	
		long dur = lastCompletion - lastStart;
		cumulExecutionTime += dur;
		if(dur > maxExecutionTime) {
			maxExecutionTime = dur;
			maxExecutionTrackingId = trackingId;
		}
		lastStart = -1;
		functionName = null;
		functionExecutionId = -1;
		trackingId = null;
	}

	public String getFunctionName()
	{
		return functionName;
	}
	
	public long getFunctionExecutionId()
	{
		return functionExecutionId;
	}
	
	public void setTrackingId(String id) 
	{
		trackingId = id;
	}
	
	public String getTrackingId()
	{
		return trackingId;
	}
		
	public void close()
	{
		synchronized(this)
		{
			quit = true;
			notify();
		}
	}

	public DataMap getStatus()
	{
		long now = System.currentTimeMillis();
		DataMap status = new DataMap();
		status.put("executing", !ready);
		if(ready) {
			status.put("idleSince", System.currentTimeMillis() - lastCompletion);
		} else {
			status.put("executingFunctionName", functionName);
			status.put("executingFunctionId", functionExecutionId);
			if(trackingId != null)
				status.put("executingTrackingId", trackingId);
			status.put("executingSince", now - lastStart);
		}
		status.put("totalExecutionCount", totalExecutionCount);
		status.put("cumulExecutionTime", cumulExecutionTime);
		if(now > threadStart)
			status.put("utilisation", (100 * cumulExecutionTime / (now - threadStart)));
		DataMap max = new DataMap();
		max.put("time", maxExecutionTime);
		max.put("trackingId", maxExecutionTrackingId);
		status.put("max", max);
		return status;
	}
}
