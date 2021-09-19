package io.firebus.script.exceptions;

import io.firebus.script.SourceInfo;

public class ScriptExecutionException extends ScriptRuntimeException {
	private static final long serialVersionUID = 1L;

	public ScriptExecutionException(String m, SourceInfo c) {
		super(m, c);
	}
	
	public ScriptExecutionException(String m, Throwable t, SourceInfo c) {
		super(m, t, c);
	}
}
