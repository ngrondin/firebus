package io.firebus.script.exceptions;

public class ScriptCallException extends ScriptRuntimeException {
	private static final long serialVersionUID = 1L;

	public ScriptCallException(String m) {
		super(m);
	}
	
	public ScriptCallException(String m, Throwable t) {
		super(m, t);
	}
}
