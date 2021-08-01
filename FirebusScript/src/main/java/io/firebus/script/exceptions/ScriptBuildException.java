package io.firebus.script.exceptions;

import io.firebus.script.SourceInfo;

public class ScriptBuildException extends ScriptException {
	private static final long serialVersionUID = -1482814057512215663L;

	public ScriptBuildException(String m) {
        super(m);
    }

    public ScriptBuildException(String m, SourceInfo c) {
		super(m, c);
	}
	
	public ScriptBuildException(String m, Throwable t, SourceInfo c) {
		super(m, t, c);
	}
}
