package io.firebus.utils;

import io.firebus.data.DataMap;
import io.firebus.logging.Logger;


public class Queue<T>
{
	protected Object[] items;
	protected int increment;
	protected boolean canGrow;
	protected int head;
	protected int tail;
	protected int depth;
	protected int count;
	protected int consumers;
	protected int maxDepth;
	protected int minConsumers;
	
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
		count = 0;
		consumers = 0;
		maxDepth = 0;
		minConsumers = 0;
	}
	
	public synchronized int push(T m)
	{
		items[head] = m;
		head++;
		depth++;
		count++;
		if(depth > maxDepth)
			maxDepth = depth;
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
				Logger.severe("fb.queue.dropped");
			}
		}
		notify();
		return depth;
	}
	
	public synchronized int getDepth()
	{
		return depth;
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
	
	public synchronized T popWait()
	{
		T item = null;
		consumers++;
		try {
			while((item = pop()) == null) {
				wait();
			}
		} catch(InterruptedException e) {
			
		} finally {
			consumers--;
			if(consumers < minConsumers)
				minConsumers = consumers;
		}
		return item;
	}
	
	public DataMap getStatus()
	{
		DataMap status = new DataMap();
		status.put("size", items.length);
		status.put("depth", getDepth());
		status.put("count", count);
		status.put("consumers", consumers);
		status.put("maxdepth", maxDepth);
		status.put("minconsumers", minConsumers);
		return status;
	}
	
	public void resetStatus() 
	{
		count = 0;
		maxDepth = getDepth();
		minConsumers = consumers;
	}
	
}
