package io.firebus.utils;

import java.util.logging.Logger;

public class Queue<T>
{
	private Logger logger = Logger.getLogger("io.firebus");
	protected Object[] items;
	protected int increment;
	protected boolean canGrow;
	protected int head;
	protected int tail;
	
	public Queue(int size)
	{
		increment = size;
		canGrow = true;
		init();
	}
	
	public Queue(int size, boolean cg)
	{
		increment = size;
		canGrow = cg;
		init();
	}
	
	protected void init() {
		items = new Object[increment];
		head = 0;
		tail = 0;		
	}
	
	public synchronized void push(T m)
	{
		items[head] = m;
		head++;
		if(head >= items.length)
			head = 0;
		if(head == tail) 
		{
			if(canGrow) 
			{
				grow();
			}
			else
			{
				tail++;
				if(tail >= items.length)
					tail = 0;
				logger.severe("Dropped message from queue");
			}
		}
	}
	
	public synchronized int getDepth()
	{
		return (((head + items.length) - tail) % items.length);
	}
	
	protected void grow() 
	{
		if(canGrow) 
		{
			Object[] newArray = new Object[items.length + increment];
			for(int i = 0; i < items.length; i++)
				newArray[i] = items[(tail + i) % items.length];
			tail = 0;
			head = items.length;
			items = newArray;
		}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized T pop()
	{
		if(head == tail) {
			return null;
		} else {
			T item = (T)items[tail];
			items[tail] = null;
			tail++;
			if(tail >= items.length)
				tail = 0;
			return item;
		}
	}	
	
}
