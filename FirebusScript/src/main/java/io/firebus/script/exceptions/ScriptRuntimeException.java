package io.firebus.script.exceptions;

import io.firebus.script.SourceInfo;

public abstract class ScriptRuntimeException extends ScriptException {
	private static final long serialVersionUID = -4728425832789172788L;


	public ScriptRuntimeException(String m) {
        super(m);
    }

	public ScriptRuntimeException(String m, Throwable t) {
		super(m, t);
	}
	
    public ScriptRuntimeException(String m, SourceInfo c) {
		super(m, c);
	}
	
	public ScriptRuntimeException(String m, Throwable t, SourceInfo c) {
		super(m, t, c);
	}
}
