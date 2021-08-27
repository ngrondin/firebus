package io.firebus;

import java.util.ArrayList;
import java.util.List;

import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.FunctionInformation;
import io.firebus.information.ServiceInformation;
import io.firebus.information.Statistics;
import io.firebus.interfaces.BusFunction;
import io.firebus.interfaces.Consumer;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.interfaces.StreamProvider;
import io.firebus.utils.DataMap;

public abstract class ExecutionManager {
	protected NodeCore nodeCore;
	
	public ExecutionManager(NodeCore nc)
	{
		nodeCore = nc;		
	}
	
	protected abstract List<FunctionEntry> getFunctionEntries();
	
	protected abstract FunctionEntry getFunctionEntry(String name);
	
	public String getFunctionStateString(int nodeId)
	{
		StringBuilder sb = new StringBuilder();
		for(FunctionEntry fe: getFunctionEntries())
		{
			BusFunction f = fe.function;
			if(f != null)
			{
				if(f instanceof ServiceProvider)
					sb.append(nodeId + ",f,s," + fe.getName() + "\r\n");
				if(f instanceof StreamProvider)
					sb.append(nodeId + ",f,t," + fe.getName() + "\r\n");
				if(f instanceof Consumer)
					sb.append(nodeId + ",f,c," + fe.getName() + "\r\n");
			}
		}
		return sb.toString();
	}
	
	
	public void processServiceInformationRequest(Message msg)
	{
		String name = msg.getSubject();
		FunctionEntry fe = getFunctionEntry(name);
		if(fe != null)
		{
			BusFunction f = fe.function;
			FunctionInformation fi = f instanceof ServiceProvider ? ((ServiceProvider)f).getServiceInformation() : f instanceof StreamProvider ? ((StreamProvider)f).getStreamInformation() : null;
			if(fi == null)
				fi = new ServiceInformation(name);
			sendMessage(msg.getOriginatorId(), msg.getCorrelation(), 0, Message.MSGTYPE_FUNCTIONINFORMATION, msg.getSubject(), new Payload(fi != null ? fi.serialise() : null));
		}
	}
	
	protected void sendError(Throwable t, int dest, int corr, int corrSeq, int msgType, String subject)
	{
		String errorMessage = "";
		Throwable c = t;
		while(c != null)
		{
			if(errorMessage.length() > 0)
				errorMessage += " : ";
			errorMessage += c.getMessage();
			c = c.getCause();
		}
		Payload payload = new Payload(errorMessage.getBytes());
		if(t instanceof FunctionErrorException) 
		{
			int code = ((FunctionErrorException)t).getErrorCode();
			if(code != 0) payload.metadata.put("errorcode", String.valueOf(code));
		}
		sendMessage(dest, corr, corrSeq, msgType, subject, payload);
	}
	
	protected void sendMessage(int dest, int corr, int corrSeq, int msgType, String subject, String body)
	{
		sendMessage(dest, corr, corrSeq, msgType, subject, new Payload(null, body != null ? body.getBytes() : null));
	}
	
	protected void sendMessage(int dest, int corr, int corrSeq, int msgType, String subject, Payload payload)
	{
		Message outMsg = new Message(dest, nodeCore.getNodeId(), msgType, subject, payload);
		outMsg.setCorrelation(corr, corrSeq);
		nodeCore.enqueue(outMsg);		
	}

	public List<Statistics> getStatistics() {
		List<Statistics> stats = new ArrayList<Statistics>();
		for(FunctionEntry fe : this.getFunctionEntries()) {
			stats.add(fe.getStatistics());
		}
		return stats;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for(FunctionEntry fe: getFunctionEntries())
		{
			sb.append(fe.getName() + "\r\n");
		}
		return sb.toString();
	}
	
	public DataMap getStatus()
	{
		DataMap status = new DataMap();
		for(FunctionEntry func: getFunctionEntries()) 
			status.put(func.getName(), func.getStatus());
		return status;
	}
	
}
