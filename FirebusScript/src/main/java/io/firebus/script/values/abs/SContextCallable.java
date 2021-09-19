package io.firebus.script.values.abs;


import io.firebus.script.exceptions.ScriptCallException;

public abstract class SContextCallable extends SCallable {

	public SValue call(SValue... arguments) throws ScriptCallException {
		return call(null, arguments);
	}
	
	public abstract SValue call(SObject thisObject, SValue... arguments) throws ScriptCallException; 

}
