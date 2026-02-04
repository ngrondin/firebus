package io.firebus.logging;

import java.io.PrintWriter;
import java.io.StringWriter;

import io.firebus.data.DataMap;
import io.firebus.threads.FirebusThread;

public class TextFormatter implements Formatter {

	public String format(int lvl, String event, String msg, DataMap data, Throwable t) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.valueOf(System.currentTimeMillis()));
		sb.append(" ");
		sb.append(pad(Logger.getLevelString(lvl), 10));
		if(Thread.currentThread() instanceof FirebusThread) {
			FirebusThread fbt = (FirebusThread)Thread.currentThread();
			sb.append(pad(fbt.getName(), 19));			
			sb.append(pad(fbt.getFunctionName(), 10));		
			if(fbt.getFunctionExecutionId() > -1)
				sb.append(pad(String.valueOf(fbt.getFunctionExecutionId()), 4));	
			else
				sb.append(pad("a", 4));
			sb.append(pad(fbt.getTrackingId(), 12));			
		} else {
			sb.append(pad(Thread.currentThread().getName(), 45));			
		}
		sb.append(pad(event, 20));
		if(msg != null)
			sb.append(msg);
		else if(data != null)
			sb.append(((DataMap)data).toString(true));
		if(t != null) {
			sb.append("\r\n");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			sb.append(sw.toString());
		}
	
		return sb.toString();
	}
	
	private String pad(String s, int l)
	{
		String ret = s != null ? s : "";
		for(int i = ret.length(); i < l; i+=1)
			ret = ret + " ";
		if(ret.length() > l)
			ret = ret.substring(0,  l);
		return ret;
	}

}
