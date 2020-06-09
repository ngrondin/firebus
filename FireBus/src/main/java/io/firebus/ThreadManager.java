package io.firebus;

import java.lang.Thread.State;
import java.util.ArrayList;

public class ThreadManager
{
	protected NodeCore nodeCore;
	protected MessageQueue queue;
	protected boolean quit;
	protected ArrayList<FirebusThread> threads;
	protected int threadCount;
	
	public ThreadManager(NodeCore c)
	{
		nodeCore = c;
		quit = false;
		threadCount = 10;
		threads = new ArrayList<FirebusThread>();
		queue = new MessageQueue(1024);
	}
	
	public void setThreadCount(int tc)
	{
		threadCount = tc;
	}
	
	public int getThreadCount()
	{
		return threadCount;
	}
	
	public synchronized void process(Message msg)
	{
		queue.push(msg);
		
		FirebusThread thread = null;
		for(int i = 0; i < threads.size(); i++)
		{
			FirebusThread t = threads.get(i);
			State s = t.getState();
			if(s == State.WAITING || s == State.TIMED_WAITING)
			{
				thread = t;
				synchronized(thread) {
					thread.notify();
				}
				break;
			}
		}
		
		if(thread == null && threads.size() < threadCount && !quit)
		{
			thread = new FirebusThread(this, nodeCore);
			threads.add(thread);
			thread.start();
		}

	}
	
	public synchronized Message getNextMessage()
	{
		return queue.pop();
	}
	
	
	public void close()
	{
		quit = true;
		for(int i = 0; i < threads.size(); i++)
			threads.get(i).close();
	}
	
}
