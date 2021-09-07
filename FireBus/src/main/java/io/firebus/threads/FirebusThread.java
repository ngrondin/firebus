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
	protected long lastStart;
	protected long lastCompletion;
	protected long cumulExecutionTime;
	protected long expiry;
	protected String functionName;
	protected long functionExecutionId = -1;
	protected String trackingId;

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
		//setName("fbThread" + getId());setName("fbThread" + getId());
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
		functionName = null;
		functionExecutionId = -1;
		lastCompletion = System.currentTimeMillis();		
		cumulExecutionTime += (lastCompletion - lastStart);
		lastStart = -1;
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
		status.put("cumulExecutionTime", cumulExecutionTime);
		if(now > threadStart)
			status.put("utilisation", (100 * cumulExecutionTime / (now - threadStart)));
		return status;
	}
}
