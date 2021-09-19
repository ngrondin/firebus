package io.firebus.script.exceptions;


public class ScriptInternalException extends ScriptRuntimeException {
	private static final long serialVersionUID = 1504839020591678429L;

    public ScriptInternalException(String m) {
		super(m);
	}
	
	public ScriptInternalException(String m, Throwable t) {
		super(m, t);
	}

}
