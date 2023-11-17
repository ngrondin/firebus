package io.firebus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import io.firebus.data.DataMap;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.logging.Logger;

public class ServiceManager extends ExecutionManager {
	protected HashMap<String, FunctionEntry> services;
	
	
	public ServiceManager(NodeCore nc)
	{
		super(nc);
		services = new HashMap<String, FunctionEntry>();
	}	
	
	public void addService(String name, ServiceProvider s, int mc)
	{
		Logger.fine("fb.service.manager.add", new DataMap("service", name));
		FunctionEntry e = services.get(name);
		if(e == null)
		{
			e = new FunctionEntry(name, s, mc);
			services.put(name, e);
		}
	}
	
	public void removeService(String name)
	{
		Logger.fine("fb.service.manager.remove", new DataMap("service", name));
		if(services.containsKey(name))
			services.remove(name);
	}
	
	public boolean hasService(String n)
	{
		return services.containsKey(n);
	}
	

	protected List<FunctionEntry> getFunctionEntries() {
		List<FunctionEntry> list = new ArrayList<FunctionEntry>();
		Iterator<String> it = services.keySet().iterator();
		while(it.hasNext())
			list.add(services.get(it.next()));
		return list;
	}

	protected FunctionEntry getFunctionEntry(String name) {
		return services.get(name);
	}


	public void executeService(Message msg)
	{
		final String name = msg.getSubject();
		final FunctionEntry fe = services.get(name);
		final Payload inPayload = msg.getPayload();
		if(fe != null)
		{
			final long executionId = fe.getExecutionId();
			if(executionId != -1) 
			{
				sendMessage(msg.getOriginatorId(), msg.getCorrelation(), 0, Message.MSGTYPE_PROGRESS, msg.getSubject(), new Payload());
				nodeCore.getServiceThreads().enqueue(new Runnable() {
					public void run() {
						Logger.fine("fb.service.manager.executing", new DataMap("service", name, "corr", msg.getCorrelation()));
						try
						{
							Payload returnPayload = ((ServiceProvider)fe.function).service(inPayload);
							Logger.fine("fb.service.manager.executed", new DataMap("service", name, "corr", msg.getCorrelation()));
							if(msg.getType() == Message.MSGTYPE_REQUESTSERVICE) 
								sendMessage(msg.getOriginatorId(), msg.getCorrelation(), 1, Message.MSGTYPE_SERVICERESPONSE, msg.getSubject(), returnPayload);
						}
						catch(Exception e)
						{
							if(msg.getType() == Message.MSGTYPE_REQUESTSERVICE) 
								sendError(e, msg.getOriginatorId(), msg.getCorrelation(), 1, Message.MSGTYPE_SERVICEERROR,  msg.getSubject());
						}
						fe.releaseExecutionId(executionId);
					}
				}, fe.getName(), executionId);
			} else {
				Logger.warning("fb.service.manager.maxreached", new DataMap("service", name, "corr", msg.getCorrelation(), "count", fe.getExecutionCount()));
				sendMessage(msg.getOriginatorId(), msg.getCorrelation(), 0, Message.MSGTYPE_FUNCTIONUNAVAILABLE, msg.getSubject(), "Maximum concurrent functions running");
			}
		}	
		else
		{
			Logger.severe("fb.service.manager.nofunction", new DataMap("service", name), null);
			sendMessage(msg.getOriginatorId(), msg.getCorrelation(), 0, Message.MSGTYPE_FUNCTIONUNAVAILABLE, msg.getSubject(), "No such function registered in this node");
		}
	}

}
