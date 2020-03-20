package io.firebus;

public class HistoryQueue
{
	protected long[] ids;
	protected int head;
	protected int tail;
	
	public HistoryQueue(int size)
	{
		ids = new long[size];
		head = 0;
		tail = 0;
	}
		
	public synchronized boolean check(long id)
	{
		int p = tail;
		while(p != head) {
			if(ids[p] == id)
				return false;
			p++;
			if(p >= ids.length)
				p = 0;
		}
		ids[head++] = id;
		if(head >= ids.length)
			head = 0;
		return true;
	}
	

	
}
