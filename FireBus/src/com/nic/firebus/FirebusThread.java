package com.nic.firebus;

public class FirebusThread extends Thread
{
	protected NodeCore nodeCore;
	protected boolean quit;
	protected Message message;

	public FirebusThread(NodeCore c)
	{
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
					message = null;
				}
				synchronized(this)
				{
					wait();
				}
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void close()
	{
		synchronized(this)
		{
			quit = true;
		}
	}

}
