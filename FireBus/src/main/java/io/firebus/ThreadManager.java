package io.firebus;

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
	
	public void process(Message msg)
	{
		FirebusThread thread = null;
		for(int i = 0; i < threads.size(); i++)
		{
			FirebusThread t = threads.get(i);
			if(!t.isBusy())
			{
				thread = t;
				break;
			}
		}
		
		if(thread == null && threads.size() < threadCount)
		{
			thread = new FirebusThread(this, nodeCore);
			threads.add(thread);
		}
		
		if(thread != null) 
		{
			thread.process(msg);
		}
		else
		{
			queue.push(msg);
		}
	}
	
	public Message getNextMessage()
	{
		return queue.pop();
	}
	
	
	public void close()
	{
		for(int i = 0; i < threads.size(); i++)
			threads.get(i).close();
	}
	
}
