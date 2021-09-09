package io.firebus.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import io.firebus.threads.FirebusThread;

public class FirebusSimpleFormatter extends Formatter
{

	public String format(LogRecord rec)
	{
		StringBuffer buf = new StringBuffer(1000);
		buf.append(rec.getMillis());
		buf.append(" ");
		buf.append(pad(rec.getLevel().toString(), 10));
		//buf.append("\t");
		//buf.append("\t");
		if(Thread.currentThread() instanceof FirebusThread) {
			FirebusThread fbt = (FirebusThread)Thread.currentThread();
			buf.append(pad(fbt.getName(), 19));			
			buf.append(pad(fbt.getFunctionName(), 10));		
			if(fbt.getFunctionExecutionId() > -1)
				buf.append(pad(String.valueOf(fbt.getFunctionExecutionId()), 4));	
			else
				buf.append(pad("a", 4));
			buf.append(pad(fbt.getTrackingId(), 12));			
			
		} else {
			buf.append(pad(Thread.currentThread().getName(), 45));			
		}
		String msg = rec.getMessage() != null ? rec.getMessage() : "";
		buf.append(msg.replaceAll("\r", "").replaceAll("\n", "\u2028"));
		buf.append("\r\n");
		return buf.toString();
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
