package io.firebus;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import io.firebus.information.FunctionInformation;
import io.firebus.information.ServiceInformation;
import io.firebus.information.StreamInformation;
import io.firebus.interfaces.BusFunction;
import io.firebus.interfaces.Consumer;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.interfaces.StreamProvider;

public class FunctionManager
{
	private Logger logger = Logger.getLogger("io.firebus");
	protected NodeCore nodeCore;
	protected HashMap<String, FunctionEntry> functions;
	protected int totalExecutionCount;
	
	public FunctionManager(NodeCore nc)
	{
		nodeCore = nc;
		functions = new HashMap<String, FunctionEntry>();
		totalExecutionCount = 0;
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
	
	public void removeFunction(String functionName)
	{
		logger.fine("Adding function to node : " + functionName);
		if(functions.containsKey(functionName))
			functions.remove(functionName);
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
				if(f instanceof StreamProvider)
					sb.append(nodeId + ",f,t," + functionName + "\r\n");
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
			FunctionInformation fi = f instanceof ServiceProvider ? ((ServiceProvider)f).getServiceInformation() : (f instanceof StreamProvider ? ((StreamProvider)f).getStreamInformation() : null);
			if(fi == null)
				fi = f instanceof ServiceProvider ? new ServiceInformation(functionName) : (f instanceof StreamProvider ? new StreamInformation(functionName) : null);
			logger.finer("Responding to a function information request");
			Message outMsg = new Message(msg.getOriginatorId(), nodeCore.getNodeId(), Message.MSGTYPE_FUNCTIONINFORMATION, msg.getSubject(), new Payload(fi != null ? fi.serialise() : null));
			outMsg.setCorrelation(msg.getCorrelation(), 0);
			nodeCore.route(outMsg);
		}
	}
	
	protected boolean canRunOneMore()
	{
		return true; //totalExecutionCount < nodeCore.getThreadManager().getThreadCount() - 2;
	}

	
	protected void sendError(Throwable t, int dest, int msgType, String subject, int corr, int corrSeq)
	{
		String errorMessage = "";
		while(t != null)
		{
			if(errorMessage.length() > 0)
				errorMessage += " : ";
			errorMessage += t.getMessage();
			t = t.getCause();
		}
		Message errorMsg = new Message(dest, nodeCore.getNodeId(), msgType, subject, new Payload(errorMessage.getBytes()));
		errorMsg.setCorrelation(corr, corrSeq);
		nodeCore.route(errorMsg);		
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
