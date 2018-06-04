package com.nic.firebus;

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
	
	/*
	public void run()
	{
		while(!quit)
		{
			try
			{
				if(queue.getMessageCount() > 0)
				{
					for(int i = 0; i < threadCount; i++)
					{
						if(!threads.get(i).isBusy())
						{
							Message msg = queue.popNextMessage();
							threads.get(i).process(msg);
						}
					}
				}
				else
				{
					synchronized(queue)
					{
						queue.wait();
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	*/
	/*
	public void close()
	{
		quit = true;
	}
	*/
}
