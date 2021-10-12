package io.firebus.script.exceptions;

import io.firebus.script.SourceInfo;

public class ScriptBuildException extends ScriptException {
	private static final long serialVersionUID = -1482814057512215663L;

	public ScriptBuildException(String m) {
        super(m);
    }

    public ScriptBuildException(String m, SourceInfo c) {
		super(enhanceMessage(m, c), c);
	}
	
	public ScriptBuildException(String m, Throwable t, SourceInfo c) {
		super(enhanceMessage(m, c), t, c);
	}
	
	public ScriptBuildException(String m, Throwable t) {
		super(m, t);
	}
	
	private static String enhanceMessage(String m, SourceInfo c) {
		if(c != null) 
			return m + " \"" + c.getText() + "\" at " + c.getLineCol();
		else
			return m;
	}
}
