package io.firebus.logging;

import java.io.PrintWriter;
import java.io.StringWriter;

import io.firebus.data.DataMap;
import io.firebus.threads.FirebusThread;

public class JSONFormatter implements Formatter {

	public String format(int lvl, String event, Object data, Throwable t) {
		DataMap entry = new DataMap();
		entry.put("ts", System.currentTimeMillis());
		entry.put("level", Logger.getLevelString(lvl));
		if(event != null) {				
			entry.put("event", event);
		}
		DataMap ctx = new DataMap();
		if(Thread.currentThread() instanceof FirebusThread) {
			FirebusThread fbt = (FirebusThread)Thread.currentThread();
			ctx.put("thread", fbt.getName());
			ctx.put("function", fbt.getFunctionName());
			if(fbt.getFunctionExecutionId() > -1)
				ctx.put("execid", fbt.getFunctionExecutionId());
			String track = fbt.getTrackingId();
			if(track != null) ctx.put("track", track);
			String user = fbt.getUser();
			if(user != null) ctx.put("user", user);
			
		} 
		entry.put("ctx", ctx);
		if(t != null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			entry.put("stack", sw.toString());
		}
		if(data != null) {
			if(data instanceof String) {
				entry.put("msg", data);
			} else if(data instanceof DataMap) {
				entry.put("data", data);				
			}				
		}		
		return entry.toString(true);
	}

}
