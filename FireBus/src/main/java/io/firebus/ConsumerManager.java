package io.firebus;
import java.util.ArrayList;
import java.util.List;

import io.firebus.data.DataMap;
import io.firebus.interfaces.Consumer;
import io.firebus.logging.Logger;

public class ConsumerManager extends ExecutionManager {
	protected List<FunctionEntry> consumers;
	
	public ConsumerManager(NodeCore nc)
	{
		super(nc);
		consumers = new ArrayList<FunctionEntry>();
	}	
	
	public void addConsumer(String name, Consumer s, int mc)
	{
		Logger.fine("fb.consumer.manager.add", new DataMap("consumer", name));
		consumers.add(new FunctionEntry(name, s, mc));
	}
	
	public void removeConsumer(String name)
	{
		Logger.fine("fb.consumer.manager.remove", new DataMap("consumer", name));
		for(int i = consumers.size(); i >= 0; i--)
			if(consumers.get(i).getName().equals(name))
				consumers.remove(i);
	}
	
	public boolean hasConsumer(String name)
	{
		for(FunctionEntry fe: consumers)
			if(fe.getName().equals(name))
				return true;
		return false;
	}
	
	protected List<FunctionEntry> getFunctionEntries() {
		return consumers;
	}

	protected FunctionEntry getFunctionEntry(String name) {
		for(FunctionEntry fe: consumers)
			if(fe.getName().equals(name))
				return fe;
		return null;
	}


	public void consume(Message msg)
	{
		final String name = msg.getSubject();
		final Payload inPayload = msg.getPayload();
		for(FunctionEntry fe: consumers)
		{
			if(fe.getName().equals(name)) {
				nodeCore.getStreamThreads().enqueue(new Runnable() {
					public void run() {
						Logger.finer("fb.consumer.executing", new DataMap("consumer", name));
						((Consumer)fe.function).consume(inPayload);
					}
				}, fe.getName(), -1);
			}
		}
	}

}
