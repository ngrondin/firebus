package io.firebus;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import io.firebus.interfaces.Consumer;

public class ConsumerManager extends ExecutionManager {
	private Logger logger = Logger.getLogger("io.firebus");
	protected List<FunctionEntry> consumers;
	
	public ConsumerManager(NodeCore nc)
	{
		super(nc);
		consumers = new ArrayList<FunctionEntry>();
	}	
	
	public void addConsumer(String name, Consumer s, int mc)
	{
		logger.fine("Adding consumer to node : " + name);
		consumers.add(new FunctionEntry(name, s, mc));
	}
	
	public void removeConsumer(String name)
	{
		logger.fine("Removing stream from node : " + name);
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
		String name = msg.getSubject();
		Payload inPayload = msg.getPayload();
		for(FunctionEntry fe: consumers)
		{
			if(fe.getName().equals(name)) {
				nodeCore.getStreamExecutionThreads().enqueue(new Runnable() {
					public void run() {
						logger.finer("Executing Consumer"); //This is not checking the function's capacity... it probably should
						((Consumer)fe.function).consume(inPayload);
					}
				}, fe.getName(), -1);
			}
		}
	}

}
