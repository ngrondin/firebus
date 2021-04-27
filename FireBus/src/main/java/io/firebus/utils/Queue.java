package io.firebus.utils;

public class Queue<T>
{
	protected Object[] items;
	protected int head;
	protected int tail;
	
	public Queue(int size)
	{
		items = new Object[size];
		head = 0;
		tail = 0;
	}
	
	public synchronized void push(T m)
	{
		items[head++] = m;
		if(head >= items.length)
			head = 0;
		if(head == tail)
			tail++;
		if(tail >= items.length)
			tail = 0;
	}
	
	public synchronized int getDepth()
	{
		return (((head + items.length) - tail) % items.length);
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
