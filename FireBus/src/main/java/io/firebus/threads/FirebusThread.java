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
	protected long functionExecutionId = -1;

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
					expiry = fbRunnable.expiry;
					fbRunnable.runnable.run();
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
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				logger.severe(sw.toString());
			}
		}
	}
	
	public void setFunctionExecutionId(long id) 
	{
		functionExecutionId = id;
	}
	
	public long getFunctionExecutionId()
	{
		return functionExecutionId;
	}
	
	public void clearFunctionExecutionId() 
	{
		functionExecutionId = -1;
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
