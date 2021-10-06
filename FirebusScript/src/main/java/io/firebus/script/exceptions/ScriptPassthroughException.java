package io.firebus.script.exceptions;

public class ScriptPassthroughException extends ScriptCallException {
	private static final long serialVersionUID = 1019065115968602634L;

	public ScriptPassthroughException(String msg) {
        super(msg);
    }

	public ScriptPassthroughException(String msg, Throwable t) {
		super(msg, t);
	}
}
