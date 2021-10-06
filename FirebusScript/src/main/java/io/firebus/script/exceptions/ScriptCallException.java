package io.firebus.script.exceptions;

public class ScriptCallException extends ScriptRuntimeException {
	private static final long serialVersionUID = 1L;
	protected String functionName;

	public ScriptCallException(String msg) {
		super(msg);
	}
	
	public ScriptCallException(String msg, String fn) {
		super(msg);
		functionName = fn;
	}
	
	public ScriptCallException(String msg, Throwable t) {
		super(msg, t);
	}

	public ScriptCallException(String msg, String fn, Throwable t) {
		super(msg, t);
		functionName = fn;
	}
	
	public String getFunctionName() {
		return functionName;
	}

}
