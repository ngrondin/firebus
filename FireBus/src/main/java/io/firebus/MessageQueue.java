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
	
	public synchronized void addMessage(Message m)
	{
		messages[head++] = m;
		if(head >= messages.length)
			head = 0;
		if(head == tail)
			tail++;
	}
	
	public synchronized int getMessageCount()
	{
		return (((head + messages.length) - tail) % messages.length);
	}
	
	public synchronized boolean checkIfContainsOrAdd(Message msg)
	{
		int p = tail;
		while(p != head) {
			if(messages[p].getOriginatorId() == msg.getOriginatorId() && messages[p].getid() == msg.getid())
				return true;
			p++;
			if(p >= messages.length)
				p = 0;
		}
		addMessage(msg);
		return false;
	}
	
	public synchronized Message popNextMessage()
	{
		if(head == tail) {
			return null;
		} else {
			Message msg = messages[tail++];
			if(tail >= messages.length)
				tail = 0;
			return msg;
		}
	}	
	
}
