package io.firebus.script.exceptions;


public class ScriptValueException extends ScriptException {
	private static final long serialVersionUID = -4728425832789172788L;

	public ScriptValueException(String m) {
        super(m);
    }
	
	public ScriptValueException(String m, Throwable t) {
		super(m, t);
	}
}
