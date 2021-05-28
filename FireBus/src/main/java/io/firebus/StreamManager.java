package io.firebus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import io.firebus.interfaces.StreamProvider;
import io.firebus.threads.FirebusThread;

public class StreamManager extends ExecutionManager {
	private Logger logger = Logger.getLogger("io.firebus");
	protected HashMap<String, FunctionEntry> streams;
	
	
	public StreamManager(NodeCore nc)
	{
		super(nc);
		streams = new HashMap<String, FunctionEntry>();
	}	
	
	public void addStream(String name, StreamProvider s, int mc)
	{
		logger.fine("Adding stream to node : " + name);
		FunctionEntry e = streams.get(name);
		if(e == null)
		{
			e = new FunctionEntry(name, s, mc);
			streams.put(name, e);
		}
	}
	
	public void removeStream(String name)
	{
		logger.fine("Removing stream from node : " + name);
		if(streams.containsKey(name))
			streams.remove(name);
	}
	
	public boolean hasStream(String n)
	{
		return streams.containsKey(n);
	}
	
	protected List<FunctionEntry> getFunctionEntries() {
		List<FunctionEntry> list = new ArrayList<FunctionEntry>();
		Iterator<String> it = streams.keySet().iterator();
		while(it.hasNext())
			list.add(streams.get(it.next()));
		return list;
	}

	protected FunctionEntry getFunctionEntry(String name) {
		return streams.get(name);
	}


	public void connectStream(Message msg)
	{
		String name = msg.getSubject();
		FunctionEntry fe = streams.get(name);
		Payload inPayload = msg.getPayload();
		if(fe != null)
		{
			long executionId = fe.getExecutionId();
			if(executionId != -1) 
			{
				nodeCore.getExecutionThreads().enqueue(new Runnable() {
					public void run() {
						logger.finer("Executing Stream Provider " + name + " (correlation: " + msg.getCorrelation() + ")");
						((FirebusThread)Thread.currentThread()).setFunctionExecutionId(executionId);
						StreamProvider streamProvider = (StreamProvider)fe.function;
						long idleTimeout = streamProvider.getStreamIdleTimeout();
						int localCorrelationId = nodeCore.getCorrelationManager().createEntry(idleTimeout);
						StreamEndpoint streamEndpoint = new StreamEndpoint(nodeCore, name, localCorrelationId, msg.getCorrelation(), 1, msg.getOriginatorId());
						streamEndpoint.setRequestPayload(inPayload);
						try
						{
							Payload acceptPayload = streamProvider.acceptStream(inPayload, streamEndpoint);
							streamEndpoint.setAcceptPayload(acceptPayload);
							logger.finer("Accepted stream " + name + " (correlation: " + msg.getCorrelation() + ")");
							if(acceptPayload == null)
								acceptPayload = new Payload();
							acceptPayload.metadata.put("correlationid", String.valueOf(localCorrelationId));
							acceptPayload.metadata.put("timeout", String.valueOf(idleTimeout));
							nodeCore.getCorrelationManager().setListenerOnEntry(localCorrelationId, streamEndpoint, idleTimeout);
							sendMessage(msg.getOriginatorId(), msg.getCorrelation(), 0, Message.MSGTYPE_STREAMACCEPT, msg.getSubject(), acceptPayload);
						}
						catch(Exception e)
						{
							sendError(e, msg.getOriginatorId(), msg.getCorrelation(), 0, Message.MSGTYPE_STREAMERROR,  msg.getSubject());
						}

						
						((FirebusThread)Thread.currentThread()).clearFunctionExecutionId();
						fe.releaseExecutionId(executionId);
					}
				});
			} else {
				logger.info("Cannot execute function " + name + " as maximum number of executions reached for this function (" + fe.getExecutionCount() + ")");
				sendMessage(msg.getOriginatorId(), msg.getCorrelation(), 0, Message.MSGTYPE_FUNCTIONUNAVAILABLE, msg.getSubject(), "Maximum concurrent functions running");
			}
		}	
		else
		{
			logger.fine("Function " + name + " does not exist");
			sendMessage(msg.getOriginatorId(), msg.getCorrelation(), 0, Message.MSGTYPE_FUNCTIONUNAVAILABLE, msg.getSubject(), "No such function registered in this node");
		}
	}
	
	
	public void logStatus() {
		StringBuilder sb = new StringBuilder();
		for(FunctionEntry fe : this.getFunctionEntries()) {
			sb.append(fe.getName());
			sb.append(":");
			sb.append(fe.getExecutionCount());
			sb.append("/");
			sb.append(fe.maxConcurrent);
			sb.append("   ");
		}
		logger.info(sb.toString());
	}

}
