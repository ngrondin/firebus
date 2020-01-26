package io.firebus;

import java.util.ArrayList;
import java.util.logging.Logger;

public class MessageQueue
{
	private Logger logger = Logger.getLogger("com.nic.firebus");
	protected ArrayList<Message> messages;
	protected ArrayList<Long[]> processed;
	
	public MessageQueue()
	{
		messages = new ArrayList<Message>();
		processed = new ArrayList<Long[]>();
	}
	
	public void addMessage(Message m)
	{
		synchronized(this)
		{
			long ct = System.currentTimeMillis();
			while(processed.size() > 0  &&  processed.get(0)[0] < ct - 60000)
				processed.remove(0);
			
			long id = (((long)m.getOriginatorId()) << 32) | ((long)m.getid());
			boolean isEcho = false;
			for(int i = 0; i < processed.size(); i++)
				if(processed.get(i)[1] == id) isEcho = true;
			
			if(isEcho == false)
			{
				messages.add(m);
				processed.add(new Long[]{ct, id});
			}
			else
			{
				logger.fine("Dropped echo message");
			}
			this.notify();
		}
	}
	
	public int getMessageCount()
	{
		return messages.size();
	}
	
	public Message popNextMessage()
	{
		synchronized(this)
		{
			Message msg = null;
			if(messages.size() > 0)
			{
				msg = messages.get(0);
				messages.remove(0);
			}
			return msg;
		}
	}

	
	
}
