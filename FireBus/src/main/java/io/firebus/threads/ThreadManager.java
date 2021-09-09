package io.firebus.threads;

import java.util.ArrayList;

import io.firebus.NodeCore;
import io.firebus.data.DataMap;
import io.firebus.utils.Queue;

public class ThreadManager extends Thread
{
	protected NodeCore nodeCore;
	protected Queue<FirebusRunnable> queue;
	protected boolean quit;
	protected ArrayList<FirebusThread> threads;
	protected int maxThreadCount;
	protected int priority;
	protected String threadName;
	
	public ThreadManager(NodeCore c, int mtc, int dp, String tn)
	{
		nodeCore = c;
		quit = false;
		maxThreadCount = mtc;
		priority = dp;
		threadName = tn;
		threads = new ArrayList<FirebusThread>();
		queue = new Queue<FirebusRunnable>(1024);
		setName("fb" + threadName + "ThreadMgr");
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
		enqueue(runnable, serviceName, serviceExecutionId, 70000);
	}
	
	public synchronized void enqueue(Runnable runnable, String serviceName, long serviceExecutionId, long timeout)
	{
		queue.push(new FirebusRunnable(runnable, serviceName, serviceExecutionId, System.currentTimeMillis() + timeout));
		
		FirebusThread thread = null;
		for(int i = 0; i < threads.size() && thread == null; i++)
		{
			FirebusThread t = threads.get(i);
			synchronized(t) {
				if(t.ready) {
					thread = t;
					t.notify();
				}
			}
		}
		
		if(thread == null && threads.size() < maxThreadCount && !quit)
		{
			String name = "fb" + threadName + "Thread" + String.format("%1$" + (maxThreadCount >= 100 ? 3 : 2) + "s", threads.size()).replace(' ', '0');
			thread = new FirebusThread(this, nodeCore);
			thread.setPriority(priority);
			thread.setName(name);
			threads.add(thread);
			thread.start();
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
	
	public DataMap getStatus()
	{
		DataMap status = new DataMap();
		DataMap tMap = new DataMap();
		for(FirebusThread thread: threads) 
			tMap.put(thread.getName(), thread.getStatus());
		status.put("threads", tMap);
		status.put("threadCount", threads.size());
		status.put("queue", queue.getStatus());
		return status;
	}
}
