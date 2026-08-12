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
	protected int minThreadCount;
	protected int priority;
	protected String threadName;
	protected long defaultTimeout;
	protected long defaultLongExecWarningTime;
	
	public ThreadManager(NodeCore c, int mintc, int maxtc, int dp, String tn, long dto, long dlewt)
	{
		nodeCore = c;
		quit = false;
		priority = dp;
		threadName = tn;
		defaultTimeout = dto;
		defaultLongExecWarningTime = dlewt;
		queue = new Queue<FirebusRunnable>(1024);
		threads = new ArrayList<FirebusThread>();
		setThreadCount(mintc, maxtc);
		setName("fb" + threadName + "ThreadMgr");
		start();
	}
	
	public void setThreadCount(int mintc, int maxtc)
	{
		minThreadCount = mintc;
		maxThreadCount = maxtc;
		while(threads.size() < minThreadCount) 
			createThread();
		while(threads.size() > maxThreadCount)
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
		enqueue(runnable, serviceName, serviceExecutionId, defaultTimeout, defaultLongExecWarningTime, null);
	}
	
	public void enqueue(Runnable runnable, String serviceName, long serviceExecutionId, long timeout, long warningtime, DataMap logData)
	{
		FirebusRunnable fbr = new FirebusRunnable(runnable, serviceName, serviceExecutionId, timeout, warningtime, logData); 
		queue.push(fbr);
		fbr.pushed = System.currentTimeMillis();
	}
	
	public FirebusRunnable getNext()
	{
		FirebusRunnable fbr = queue.popWait();
		if(fbr != null)
			fbr.poped = System.currentTimeMillis();
		return fbr;
	}
	
	public void run() 
	{
		while(queue.getConsumers() < threads.size()) 
			try	{Thread.sleep(100); } catch(Exception e) {} // For all threads to be connected before managing
		
		while(!quit) 
		{
			try 
			{
				Thread.sleep(1000);
				for(int i = 0; i < threads.size(); i++) 
					threads.get(i).checkExpiry();
				if(queue.getConsumers() == 0 && threads.size() < maxThreadCount) 
					createThread();
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
		setThreadCount(0, 0);
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
