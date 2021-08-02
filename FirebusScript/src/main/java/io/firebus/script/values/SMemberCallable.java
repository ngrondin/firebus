package io.firebus.script.values;


import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SObject;
import io.firebus.script.values.abs.SValue;

public class SMemberCallable extends SCallable {
	protected SObject object;
	protected SCallable callable;
	
	public SMemberCallable(SObject o, SCallable c) {
		super();
		object = o;
		callable = c;
	}
	
	public SValue call(SValue[] arguments) throws ScriptException {
		if(callable instanceof SInternalCallable) {
			return ((SInternalCallable)callable).call(object, arguments);
		} else {
			return callable.call(arguments);
		}
	}

}
