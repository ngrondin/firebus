package io.firebus.utils;

import java.util.logging.Logger;

import io.firebus.data.DataMap;


public class Queue<T>
{
	private Logger logger = Logger.getLogger("io.firebus");
	protected Object[] items;
	protected int increment;
	protected boolean canGrow;
	protected int head;
	protected int tail;
	protected int depth;
	protected int max;
	
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
		depth = 0;
		max = 0;
	}
	
	public synchronized void push(T m)
	{
		items[head] = m;
		head++;
		depth++;
		if(depth > max)
			max = depth;
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
				depth--;
				logger.severe("Dropped message from queue");
			}
		}
		notify();
	}
	
	public synchronized int getDepth()
	{
		return depth;
		//return (((head + items.length) - tail) % items.length);
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
			depth--;
			if(tail >= items.length)
				tail = 0;
			return item;
		}
	}	
	
	public DataMap getStatus()
	{
		DataMap status = new DataMap();
		status.put("depth", getDepth());
		status.put("size", items.length);
		status.put("max", max);
		return status;
	}
	
}
