package io.firebus;

import java.util.logging.Logger;

public class FirebusThread extends Thread
{
	private Logger logger = Logger.getLogger("io.firebus");
	protected NodeCore nodeCore;
	protected ThreadManager threadManager;
	protected boolean quit;
	protected Message message;

	public FirebusThread(ThreadManager tm, NodeCore c)
	{
		threadManager = tm;
		nodeCore = c;
		quit = false;
		setName("fbThread" + getId());
	}
	
	public boolean isBusy()
	{
		return (message != null);
	}
		
	public void run()
	{
		while(!quit)
		{
			try
			{
				message = threadManager.getNextMessage();
				if(message != null)
				{
					nodeCore.route(message);
				}
				else
				{
					synchronized(this)
					{
						if(!quit)
							wait();
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
