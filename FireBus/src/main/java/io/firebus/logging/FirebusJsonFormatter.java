package io.firebus.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import io.firebus.data.DataMap;
import io.firebus.threads.FirebusThread;

public class FirebusJsonFormatter extends Formatter
{

	public String format(LogRecord rec)
	{
		DataMap map = new DataMap();
		map.put("ts", rec.getMillis());
		map.put("level", rec.getLevel().toString());
		if(Thread.currentThread() instanceof FirebusThread) {
			FirebusThread fbt = (FirebusThread)Thread.currentThread();
			map.put("thread", fbt.getName());
			map.put("function", fbt.getFunctionName());
			if(fbt.getFunctionExecutionId() > -1)
				map.put("execid", fbt.getFunctionExecutionId());
			String track = fbt.getTrackingId();
			if(track != null) map.put("track", track);
			String user = fbt.getUser();
			if(user != null) map.put("user", user);
			
		} 
		Object[] params = rec.getParameters();
		if(params != null) {
			for(Object param: params) {
				if(param instanceof DataMap) {
					DataMap paramMap = (DataMap)param;
					for(String key: paramMap.keySet()) {
						map.put(key, paramMap.get(key));
					}
				}
			}			
		}
		Throwable t = rec.getThrown();
		if(t != null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			map.put("stack", sw.toString());
		}
		String msg = rec.getMessage() != null ? rec.getMessage() : "";
		map.put("msg", msg.replaceAll("\r", "").replaceAll("\n", "\u2028"));
		return map.toString(true) + "\r\n";
	}

}
