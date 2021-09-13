package io.firebus.script.exceptions;

import io.firebus.script.SourceInfo;

public class ScriptException extends Exception {
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
		return super.getMessage() + (context != null ? " [" + context.getLineCol() + "]" : "");
	}
	
	public String getMessageText() {
		return super.getMessage();
	}
	
	public SourceInfo getSourceInfo() {
		return context;
	}
}
