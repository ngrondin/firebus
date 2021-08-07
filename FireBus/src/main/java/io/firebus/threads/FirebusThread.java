package io.firebus.threads;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import io.firebus.NodeCore;

public class FirebusThread extends Thread
{
	private Logger logger = Logger.getLogger("io.firebus");
	protected NodeCore nodeCore;
	protected ThreadManager threadManager;
	protected boolean quit;
	protected boolean ready;
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
		setName("fbThread" + getId());
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
					startFunctionExecution(fbRunnable.functionName, fbRunnable.functionExecutionId);
					expiry = fbRunnable.expiry;
					fbRunnable.runnable.run();
					finishFunctionExecution();
				}
				else
				{
					synchronized(this)
					{
						if(!quit) 
						{
							expiry = -1;
							ready = true;
							wait();
							ready = false;
							logger.finest("Thread " + getId() + " has just woken up");
						}
					}
				}
			} 
			catch (Exception e)
			{
				finishFunctionExecution();
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				logger.severe(sw.toString());
			}
		}
	}
	
	public void startFunctionExecution(String n, long id)
	{
		functionName = n;
		functionExecutionId = id;
	}
	
	public void finishFunctionExecution() 
	{
		functionName = null;
		functionExecutionId = -1;
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

}
