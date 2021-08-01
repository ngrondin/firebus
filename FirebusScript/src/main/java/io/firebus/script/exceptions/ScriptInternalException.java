package io.firebus.script.exceptions;

import io.firebus.script.SourceInfo;

public class ScriptInternalException extends ScriptException {
	private static final long serialVersionUID = 1504839020591678429L;

	public ScriptInternalException(String m) {
        super(m);
    }

    public ScriptInternalException(String m, SourceInfo c) {
		super(m, c);
	}
	
	public ScriptInternalException(String m, Throwable t, SourceInfo c) {
		super(m, t, c);
	}

}
