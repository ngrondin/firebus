package io.firebus.script.exceptions;

import io.firebus.script.SourceInfo;

public class BuildException extends ScriptException {

    public BuildException(String m) {
        super(m);
    }

    public BuildException(String m, SourceInfo c) {
		super(m, c);
	}
	
	public BuildException(String m, Throwable t, SourceInfo c) {
		super(m, t, c);
	}
}
