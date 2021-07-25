package io.firebus.script;

import io.firebus.script.units.UnitContext;

public class ScriptException extends Exception {
	private static final long serialVersionUID = 1L;
	protected UnitContext context;

	public ScriptException(String m, UnitContext c) {
		super(m);
		context = c;
	}
	
	public ScriptException(String m, Throwable t, UnitContext c) {
		super(m, t);
		context = c;
	}
	
	public String getMessage() {
		return super.getMessage() + " [" + context.toString() + "]";
	}
}
