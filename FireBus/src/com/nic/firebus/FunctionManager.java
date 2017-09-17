package com.nic.firebus;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionUnavailableException;
import com.nic.firebus.information.ConsumerInformation;
import com.nic.firebus.information.FunctionInformation;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.interfaces.BusFunction;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.interfaces.FunctionListener;
import com.nic.firebus.interfaces.Publisher;
import com.nic.firebus.interfaces.ServiceProvider;

public class FunctionManager implements FunctionListener
{
	protected class FunctionEntry
	{
		protected ServiceInformation serviceInformation;
		protected ConsumerInformation consumerInformation;
		protected BusFunction function;
		protected int maxConcurrent;
		protected int currentCount;
		
		public FunctionEntry(BusFunction f, int mc)
		{
			function = f;
			maxConcurrent = mc;
		}
		
		public void setServiceInformation(ServiceInformation si)
		{
			serviceInformation = si;
		}
		
		public void setConsumerInformation(ConsumerInformation ci)
		{
			consumerInformation = ci;
		}
	}
	
	private Logger logger = Logger.getLogger("com.nic.firebus");
	protected NodeCore nodeCore;
	protected HashMap<String, FunctionEntry> functions;
	
	public FunctionManager(NodeCore nc)
	{
		nodeCore = nc;
		functions = new HashMap<String, FunctionEntry>();
	}
	
	public void addFunction(FunctionInformation fi, BusFunction f, int mc)
	{
		String functionName = fi.getName();
		FunctionEntry e = functions.get(functionName);
		if(e == null)
		{
			e = new FunctionEntry(f, mc);
			functions.put(fi.getName(), e);
		}
		if(fi instanceof ServiceInformation)
			e.setServiceInformation((ServiceInformation)fi);
		else
			e.setConsumerInformation((ConsumerInformation)fi);
	}
	
	public boolean hasFunction(String n)
	{
		return functions.containsKey(n);
	}
	
	public String getFunctionStateString(int nodeId)
	{
		StringBuilder sb = new StringBuilder();
		Iterator<String> it = functions.keySet().iterator();
		while(it.hasNext())
		{
			String functionName = it.next();
			BusFunction f = functions.get(functionName).function;
			if(f != null)
			{
				if(f instanceof ServiceProvider)
					sb.append(nodeId + ",f,s," + functionName + "\r\n");
				if(f instanceof Publisher)
					sb.append(nodeId + ",f,p," + functionName + "\r\n");
				if(f instanceof Consumer)
					sb.append(nodeId + ",f,c," + functionName + "\r\n");
			}
		}
		return sb.toString();
	}
	
	public ServiceInformation getServiceInformation(String functionName)
	{
		if(functions.containsKey(functionName))
			return functions.get(functionName).serviceInformation;
		else
			return null;
	}
	
	public void executeFunction(Message msg) throws FunctionUnavailableException
	{
		logger.fine("Executing Function");
		String functionName = msg.getSubject();
		FunctionEntry fe = functions.get(functionName);
		if(fe != null)
		{
			BusFunction f = fe.function;
			if(fe.currentCount < fe.maxConcurrent)
			{
				new FunctionWorker(f, msg, this);
				fe.currentCount++;
			}
			else
			{
				throw new FunctionUnavailableException("Maximum concurrent functions running");
			}
		}	
		else
		{
			throw new FunctionUnavailableException("No such function registered in this node");
		}
	}
	
	public void functionCallback(Message inboundMessage, Payload payload)
	{
		logger.fine("Function Returned");
		String functionName = inboundMessage.getSubject();
		FunctionEntry fe = functions.get(functionName);
		if(fe != null)
			fe.currentCount--;

		Message msg = new Message(inboundMessage.getOriginatorId(), nodeCore.getNodeId(), Message.MSGTYPE_SERVICERESPONSE, inboundMessage.getSubject(), payload);
		msg.setCorrelation(inboundMessage.getCorrelation());
		nodeCore.sendMessage(msg);
	}
	
	public void functionErrorCallback(Message inboundMessage, FunctionErrorException error) 
	{
		String functionName = inboundMessage.getSubject();
		FunctionEntry fe = functions.get(functionName);
		if(fe != null)
			fe.currentCount--;

		Message msg = new Message(inboundMessage.getOriginatorId(), nodeCore.getNodeId(), Message.MSGTYPE_SERVICEERROR, inboundMessage.getSubject(), new Payload(null, error.getMessage().getBytes()));
		msg.setCorrelation(inboundMessage.getCorrelation());
		nodeCore.sendMessage(msg);
	}


	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		Iterator<String> it = functions.keySet().iterator();
		while(it.hasNext())
		{
			String fn = it.next();
			sb.append(fn + "  " + functions.get(fn) + "\r\n");
		}
		return sb.toString();
	}

}
