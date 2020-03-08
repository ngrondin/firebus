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
		start();
	}
	
	public boolean isBusy()
	{
		return (message != null);
	}
	
	public void process(Message m)
	{
		message = m;
		synchronized(this)
		{
			this.notify();
		}
	}
	
	public void run()
	{
		while(!quit)
		{
			try
			{
				if(message != null)
				{
					nodeCore.route(message);
					message = threadManager.getNextMessage();
				}
				if(message == null) 
				{
					synchronized(this)
					{
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
