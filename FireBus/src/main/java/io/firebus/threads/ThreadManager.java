package io.firebus.threads;

import java.util.ArrayList;
import java.util.logging.Logger;

import io.firebus.NodeCore;
import io.firebus.utils.Queue;

public class ThreadManager extends Thread
{
	private static Logger logger = Logger.getLogger("io.firebus");
	
	protected NodeCore nodeCore;
	protected Queue<FirebusRunnable> queue;
	protected boolean quit;
	protected ArrayList<FirebusThread> threads;
	protected int maxThreadCount;
	
	public ThreadManager(NodeCore c)
	{
		nodeCore = c;
		quit = false;
		maxThreadCount = 10;
		threads = new ArrayList<FirebusThread>();
		queue = new Queue<FirebusRunnable>(1024);
		start();
	}
	
	public void setMaxThreadCount(int tc)
	{
		maxThreadCount = tc;
	}
	
	public int getThreadCount()
	{
		return maxThreadCount;
	}
	
	public int getQueueDepth()
	{
		return queue.getDepth();
	}
	
	public void enqueue(Runnable runnable, String serviceName, long serviceExecutionId)
	{
		enqueue(runnable, serviceName, serviceExecutionId, 30000);
	}
	
	public synchronized void enqueue(Runnable runnable, String serviceName, long serviceExecutionId, long timeout)
	{
		queue.push(new FirebusRunnable(runnable, serviceName, serviceExecutionId, System.currentTimeMillis() + timeout));
		logger.finest("Dropped runnable in thread queue (depth: " + queue.getDepth() + ")");
		
		FirebusThread thread = null;
		for(int i = 0; i < threads.size(); i++)
		{
			FirebusThread t = threads.get(i);
			State s = t.getState();
			if(s == State.WAITING && t.ready)
			{
				thread = t;
				synchronized(thread) {
					logger.finest("Notifying thread " + thread.getId() + " to start");
					thread.notify();
				}
				break;
			}
		}
		
		if(thread == null && threads.size() < maxThreadCount && !quit)
		{
			thread = new FirebusThread(this, nodeCore);
			threads.add(thread);
			thread.start();
			logger.finest("Added thread " + thread.getId() + " ");
		}

	}
	
	public synchronized FirebusRunnable getNext()
	{
		return queue.pop();
	}
	
	public void run() 
	{
		while(!quit) 
		{
			try 
			{
				long now = System.currentTimeMillis();
				for(int i = 0; i < threads.size(); i++)
				{
					FirebusThread t = threads.get(i);
					State s = t.getState();
					if(s == State.WAITING && !t.ready && t.expiry < now)
					{
						t.interrupt();
					}
				}				
				Thread.sleep(2000);
			} 
			catch(Exception e) {
				
			}
		}
	}
	
	public void close()
	{
		quit = true;
		for(int i = 0; i < threads.size(); i++)
			threads.get(i).close();
	}
	
}
