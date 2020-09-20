package io.firebus;

public class MessageQueue
{
	protected Message[] messages;
	protected int head;
	protected int tail;
	
	public MessageQueue(int size)
	{
		messages = new Message[size];
		head = 0;
		tail = 0;
	}
	
	public synchronized void push(Message m)
	{
		messages[head++] = m;
		if(head >= messages.length)
			head = 0;
		if(head == tail)
			tail++;
		if(tail >= messages.length)
			tail = 0;
	}
	
	public synchronized int getMessageCount()
	{
		return (((head + messages.length) - tail) % messages.length);
	}
	
	
	public synchronized Message pop()
	{
		if(head == tail) {
			return null;
		} else {
			Message msg = messages[tail];
			messages[tail] = null;
			tail++;
			if(tail >= messages.length)
				tail = 0;
			return msg;
		}
	}	
	
}
