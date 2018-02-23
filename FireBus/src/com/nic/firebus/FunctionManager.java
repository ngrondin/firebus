package com.nic.firebus;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.interfaces.BusFunction;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.interfaces.Publisher;
import com.nic.firebus.interfaces.ServiceProvider;

public class FunctionManager
{
	private Logger logger = Logger.getLogger("com.nic.firebus");
	protected NodeCore nodeCore;
	protected HashMap<String, FunctionEntry> functions;
	
	public FunctionManager(NodeCore nc)
	{
		nodeCore = nc;
		functions = new HashMap<String, FunctionEntry>();
	}
	
	public void addFunction(String functionName, BusFunction f, int mc)
	{
		logger.fine("Adding function to node : " + functionName);
		FunctionEntry e = functions.get(functionName);
		if(e == null)
		{
			e = new FunctionEntry(functionName, f, mc);
			functions.put(functionName, e);
		}
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
	
	public void processServiceInformationRequest(Message msg)
	{
		String functionName = msg.getSubject();
		if(functions.containsKey(functionName))
		{
			BusFunction f = functions.get(functionName).function;
			if(f instanceof ServiceProvider)
			{
				ServiceInformation si =  ((ServiceProvider)f).getServiceInformation();
				if(si == null)
					si = new ServiceInformation(functionName);
				logger.fine("Responding to a service information request");
				Message outMsg = new Message(msg.getOriginatorId(), nodeCore.getNodeId(), Message.MSGTYPE_SERVICEINFORMATION, msg.getSubject(), new Payload(si.serialise()));
				outMsg.setCorrelation(msg.getCorrelation());
				nodeCore.sendMessage(outMsg);
			}
		}
	}

	
	public void executeFunction(Message msg)
	{
		logger.fine("Executing Function");
		String functionName = msg.getSubject();
		FunctionEntry fe = functions.get(functionName);
		if(fe != null)
		{
			if(fe.canRunOneMore())
			{
				new FunctionWorker(fe, msg, nodeCore);
			}
			else
			{
				Message outMsg = new Message(msg.getOriginatorId(), nodeCore.getNodeId(), Message.MSGTYPE_SERVICEUNAVAILABLE, msg.getSubject(),new Payload(null,  "Maximum concurrent functions running".getBytes()));
				outMsg.setCorrelation(msg.getCorrelation());
				nodeCore.sendMessage(outMsg);
			}
		}	
		else
		{
			Message outMsg = new Message(msg.getOriginatorId(), nodeCore.getNodeId(), Message.MSGTYPE_SERVICEUNAVAILABLE, msg.getSubject(),new Payload(null,  "No such function registered in this node".getBytes()));
			outMsg.setCorrelation(msg.getCorrelation());
			nodeCore.sendMessage(outMsg);
		}
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
