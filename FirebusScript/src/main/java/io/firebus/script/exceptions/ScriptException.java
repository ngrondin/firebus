package io.firebus.script.exceptions;

import java.util.ArrayList;
import java.util.List;

import io.firebus.script.SourceInfo;

public abstract class ScriptException extends Exception {
	private static final long serialVersionUID = 1L;
	protected SourceInfo context;

	public ScriptException(String m) {
		super(m);
	}
	
	public ScriptException(String m, SourceInfo c) {
		super(m);
		context = c;
	}
	
	public ScriptException(String m, Throwable t) {
		super(m, t);
	}
	
	public ScriptException(String m, Throwable t, SourceInfo c) {
		super(m, t);
		context = c;
	}
	
	public String getMessage() {
		return super.getMessage()/* + (context != null ? " [" + context.getLineCol() + "]" : "")*/;
	}
	
	public String getMessageText() {
		return super.getMessage();
	}
	
	public SourceInfo getSourceInfo() {
		return context;
	}
	
	
	public static ScriptException flatten(ScriptExecutionException e, Class<?> c) {
		List<StackTraceElement> list = new ArrayList<StackTraceElement>();
		StackTraceElement[] nextElements = e.getStackTrace();
		int i = 0;
		while(i < nextElements.length && !nextElements[i].getClassName().equals(c.getName())) i++;
		for(; i < nextElements.length; i++)
			list.add(nextElements[i]);
		Throwable t = e;
		String functionName = "main";
		ScriptException lastScriptException = null;
		while(t != null && t instanceof ScriptException) {
			lastScriptException = (ScriptException)t;
			if(t instanceof ScriptExecutionException) {
				ScriptExecutionException see = (ScriptExecutionException)t;
				list.add(0, new StackTraceElement("<script>", functionName != null ? functionName : "", see.getSourceInfo().getSourceName(), see.getSourceInfo().getLine()));
			} else if(t != null && t instanceof ScriptCallException) {
				ScriptCallException sce = (ScriptCallException)t;
				functionName = sce.getFunctionName();
			} 
			t = t.getCause();
		}
		StackTraceElement[] stea = list.toArray(new StackTraceElement[] {});
		lastScriptException.setStackTrace(stea);
		return lastScriptException;
	}
}
