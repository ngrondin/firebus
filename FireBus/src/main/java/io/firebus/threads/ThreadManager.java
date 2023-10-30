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
	protected int threadCount;
	protected int priority;
	protected String threadName;
	
	public ThreadManager(NodeCore c, int tc, int dp, String tn)
	{
		nodeCore = c;
		quit = false;
		priority = dp;
		threadName = tn;
		queue = new Queue<FirebusRunnable>(1024);
		threads = new ArrayList<FirebusThread>();
		setThreadCount(tc);
		setName("fb" + threadName + "ThreadMgr");
		start();
	}
	
	public void setThreadCount(int tc)
	{
		threadCount = tc;	
		while(threads.size() < threadCount) 
			createThread();
		while(threads.size() > threadCount)
			removeThread();
	}
	
	public int getThreadCount()
	{
		return threads.size();
	}
	
	
	public int getQueueDepth()
	{
		return queue.getDepth();
	}
	
	public void enqueue(Runnable runnable, String serviceName, long serviceExecutionId)
	{
		enqueue(runnable, serviceName, serviceExecutionId, 70000);
	}
	
	public void enqueue(Runnable runnable, String serviceName, long serviceExecutionId, long timeout)
	{
		queue.push(new FirebusRunnable(runnable, serviceName, serviceExecutionId, timeout));
	}
	
	public FirebusRunnable getNext()
	{
		return queue.popWait();
	}
	
	public void run() 
	{
		boolean first = true;
		while(!quit) 
		{
			try 
			{
				Thread.sleep(1000);
				if(first) {
					queue.resetStatus(); //This is to reset the minConsumer stat once all threads have connected
					first = false;
				}
				for(int i = 0; i < threads.size(); i++) 
					threads.get(i).checkExpiry();
			} 
			catch(Exception e) { }
		}
	}
	
	private void createThread()
	{
		int id = threads.size();
		FirebusThread thread = new FirebusThread(this, nodeCore, id, threadName, priority);
		threads.add(thread);
	}
	
	private void removeThread() 
	{
		if(threads.size() > 0) {
			int id = threads.size() - 1;
			FirebusThread thread = threads.get(id);
			threads.remove(id);
			thread.close();
		}
	}
	
	public void close()
	{
		quit = true;
		setThreadCount(0);
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
