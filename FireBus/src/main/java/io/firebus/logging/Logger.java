package io.firebus.logging;

import io.firebus.data.DataMap;

public class Logger {
	protected static int level = 0;
	
	protected static Formatter formatter = new JSONFormatter();
	
	public static void setLevel(int l) {
		level = l;
	}
	
	public static void setFormatter(String type) {
		if(type.toLowerCase().equals("json"))
			formatter = new JSONFormatter();
		else if(type.equalsIgnoreCase("text")) 
			formatter = new TextFormatter();
	}
	
	public static void log(int lvl, String event, String msg, DataMap data, Throwable t) {
		if(lvl <= level) {
			String line = formatter.format(lvl, event, msg, data, t);

			if(lvl <= 1) {
				System.err.println(line);
			} else {
				System.out.println(line);
			}
		}
	}
	
	public static void severe(String event, String msg, DataMap data, Throwable t) {
		log(Level.SEVERE, event, msg, data, t);
	}
	
	public static void severe(String event, String msg, Throwable t) {
		log(Level.SEVERE, event, msg, null, t);
	}

	public static void severe(String event, DataMap data, Throwable t) {
		log(Level.SEVERE, event, null, data, t);
	}
	
	public static void severe(String event, DataMap data) {
		log(Level.SEVERE, event, null, data, null);
	}
	
	public static void severe(String event, Throwable t) {
		log(Level.SEVERE, event, null, null, t);
	}
	
	public static void severe(String event) {
		log(Level.SEVERE, event, null, null, null);
	}
	
	public static void warning(String event, String msg, DataMap data, Throwable t) {
		log(Level.WARNING, event, msg, data, t);
	}
	
	public static void warning(String event, String msg, Throwable t) {
		log(Level.WARNING, event, msg, null, t);
	}

	public static void warning(String event, DataMap data, Throwable t) {
		log(Level.WARNING, event, null,  data, t);
	}
	
	public static void warning(String event, String msg) {
		log(Level.WARNING, event, msg, null, null);
	}
	
	public static void warning(String event, DataMap data) {
		log(Level.WARNING, event, null, data, null);
	}
	
	public static void warning(String event, Throwable t) {
		log(Level.WARNING, event, null, null, t);
	}
	
	public static void warning(String event) {
		log(Level.WARNING, event, null, null, null);
	}
	
	public static void security(String event, String msg, DataMap data, Throwable t) {
		log(Level.SECURITY, event, msg, data, t);
	}

	public static void security(String event, String msg, Throwable t) {
		log(Level.SECURITY, event, msg, null, t);
	}
	
	public static void security(String event, DataMap data, Throwable t) {
		log(Level.SECURITY, event, null, data, t);
	}
	
	public static void security(String event, DataMap data) {
		log(Level.SECURITY, event, null, data, null);
	}
	
	public static void security(String event, Throwable t) {
		log(Level.SECURITY, event, null, null, t);
	}
	
	public static void security(String event) {
		log(Level.SECURITY, event, null, null, null);
	}
	
	public static void userError(String event, String msg, DataMap data, Throwable t) {
		log(Level.USERERROR, event, msg, data, t);
	}
	
	public static void userError(String event, String msg, Throwable t) {
		log(Level.USERERROR, event, msg, null, t);
	}
	
	public static void userError(String event, DataMap data, Throwable t) {
		log(Level.USERERROR, event, null, data, t);
	}

	public static void userError(String event, String msg, DataMap data) {
		log(Level.USERERROR, event, msg, data, null);
	}
	
	public static void userError(String event, DataMap data) {
		log(Level.USERERROR, event, null, data, null);
	}

	public static void userError(String event, String msg) {
		log(Level.USERERROR, event, msg, null, null);
	}

	public static void userError(String event, Throwable t) {
		log(Level.USERERROR, event, null, null, t);
	}
	
	public static void userError(String event) {
		log(Level.USERERROR, event, null, null, null);
	}
	
	public static void info(String event, String msg) {
		log(Level.INFO, event, msg, null, null);
	}
	
	public static void info(String event, DataMap data) {
		log(Level.INFO, event, null, data, null);
	}
	
	public static void info(String event) {
		log(Level.INFO, event, null, null, null);
	}

	public static void fine(String event, String msg) {
		log(Level.FINE, event, msg, null, null);
	}
	
	public static void fine(String event, DataMap data) {
		log(Level.FINE, event, null, data, null);
	}
	
	public static void fine(String event) {
		log(Level.FINE, event, null, null, null);
	}

	public static void finer(String event, String msg) {
		log(Level.FINER, event, msg, null, null);
	}
	
	public static void finer(String event, DataMap data) {
		log(Level.FINER, event, null, data, null);
	}
	
	public static void finer(String event) {
		log(Level.FINER, event, null, null, null);
	}

	public static void finest(String event, DataMap data) {
		log(Level.FINEST, event, null, data, null);
	}
	
	public static void finest(String event) {
		log(Level.FINEST, event, null, null, null);
	}

	public static int getLevel() {
		return level;
	}
	
	public static boolean isLevel(int lvl) {
		return lvl <= level;
	}
	
	public static String getLevelString(int lvl) {
		switch(lvl) {
		case Level.SEVERE: return "SEVERE";
		case Level.WARNING: return "WARNING";
		case Level.SECURITY: return "SECURITY";
		case Level.USERERROR: return "USERERROR";
		case Level.INFO: return "INFO";
		case Level.FINE: return "FINE";
		case Level.FINER: return "FINER";
		case Level.FINEST: return "FINEST";
		}
		return "";
	}
	
	public static int getLevelFromString(String s) {
		switch(s) {
		case "SEVERE": return Level.SEVERE;
		case "WARNING": return Level.WARNING;
		case "SECURITY": return Level.SECURITY;
		case "USERERROR": return Level.USERERROR;
		case "INFO": return Level.INFO;
		case "FINE": return Level.FINE;
		case "FINER": return Level.FINER;
		case "FINEST": return Level.FINEST;
		}
		return -1;
	}
}
