package io.firebus;

import java.util.ArrayList;

public class ThreadManager
{
	protected NodeCore nodeCore;
	//protected MessageQueue queue;
	protected boolean quit;
	protected ArrayList<FirebusThread> threads;
	protected int threadCount;
	
	public ThreadManager(NodeCore c)
	{
		nodeCore = c;
		//queue = q;
		quit = false;
		threadCount = 10;
		threads = new ArrayList<FirebusThread>();
		for(int i = 0; i < threadCount; i++)
			threads.add(new FirebusThread(nodeCore));
	}
	
	public void startThread(Message msg)
	{
		boolean foundThread = false;
		for(int i = 0; i < threads.size(); i++)
		{
			if(!threads.get(i).isBusy())
			{
				foundThread = true;
				threads.get(i).process(msg);
				break;
			}
		}
		if(foundThread == false)
		{
			FirebusThread t = new FirebusThread(nodeCore);
			threads.add(t);
			t.process(msg);
		}
	}
	
	
	public void close()
	{
		for(int i = 0; i < threads.size(); i++)
			threads.get(i).close();
	}
	
}
