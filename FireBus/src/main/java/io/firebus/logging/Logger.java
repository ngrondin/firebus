package io.firebus.logging;

import java.io.PrintWriter;
import java.io.StringWriter;

import io.firebus.data.DataMap;
import io.firebus.threads.FirebusThread;

public class Logger {
	public static int level = 0;
	
	public static void setLevel(int l) {
		level = l;
	}
	
	public static void log(int lvl, String event, Object data, Throwable t) {
		if(lvl <= level) {
			DataMap entry = new DataMap();
			entry.put("ts", System.currentTimeMillis());
			entry.put("level", getLevelString(lvl));
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
			if(lvl <= 1) {
				System.err.println(entry.toString(0, true));
			} else {
				System.out.println(entry.toString(0, true));
			}
		}
		
	}
	
	public static void severe(String event, Object data, Throwable t) {
		log(Level.SEVERE, event, data, t);
	}
	
	public static void severe(String event, Object data) {
		log(Level.SEVERE, event, data, null);
	}
	
	public static void severe(String event, Throwable t) {
		log(Level.SEVERE, event, null, t);
	}
	
	public static void severe(String event) {
		log(Level.SEVERE, event, null, null);
	}
	
	public static void warning(String event, Object data, Throwable t) {
		log(Level.WARNING, event, data, t);
	}
	
	public static void warning(String event, Object data) {
		log(Level.WARNING, event, data, null);
	}
	
	public static void warning(String event, Throwable t) {
		log(Level.WARNING, event, null, t);
	}
	
	public static void warning(String event) {
		log(Level.WARNING, event, null, null);
	}
	
	public static void info(String event, Object data) {
		log(Level.INFO, event, data,null);
	}
	
	public static void info(String event) {
		log(Level.INFO, event, null, null);
	}

	public static void fine(String event, Object data) {
		log(Level.FINE, event, data,null);
	}
	
	public static void fine(String event) {
		log(Level.FINE, event, null, null);
	}

	public static void finer(String event, Object data) {
		log(Level.FINER, event, data,null);
	}
	
	public static void finer(String event) {
		log(Level.FINER, event, null, null);
	}

	public static void finest(String event, Object data) {
		log(Level.FINEST, event, data,null);
	}
	
	public static void finest(String event) {
		log(Level.FINEST, event, null, null);
	}

	
	public static String getLevelString(int lvl) {
		switch(lvl) {
		case 0: return "SEVERE";
		case 1: return "WARNING";
		case 2: return "INFO";
		case 3: return "FINE";
		case 4: return "FINER";
		case 5: return "FINEST";
		}
		return "";
	}
	
	public static int getLevelFromString(String s) {
		switch(s) {
		case "SEVERE": return 0;
		case "WARNING": return 1;
		case "INFO": return 2;
		case "FINE": return 3;
		case "FINER": return 4;
		case "FINEST": return 5;
		}
		return -1;
	}
}
