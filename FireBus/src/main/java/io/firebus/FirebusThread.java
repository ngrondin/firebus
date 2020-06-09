package io.firebus;

import java.util.logging.Logger;

public class FirebusThread extends Thread
{
	private Logger logger = Logger.getLogger("io.firebus");
	protected NodeCore nodeCore;
	protected ThreadManager threadManager;
	protected boolean quit;
	protected boolean ready;

	public FirebusThread(ThreadManager tm, NodeCore c)
	{
		threadManager = tm;
		nodeCore = c;
		quit = false;
		ready = false;
		setName("fbThread" + getId());
	}
	
		
	public void run()
	{
		while(!quit)
		{
			try
			{
				Message message = threadManager.getNextMessage();
				if(message != null)
				{
					nodeCore.route(message);
				}
				else
				{
					synchronized(this)
					{
						if(!quit) 
						{
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
				logger.severe(e.getMessage());
				e.printStackTrace();
			}
		}
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
