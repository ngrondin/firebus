package io.firebus.script.values;


import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SContextCallable;
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
	
	public SValue call(SValue... arguments) throws ScriptCallException {
		if(callable instanceof SContextCallable) {
			return ((SContextCallable)callable).call(object, arguments);
		} else {
			return callable.call(arguments);
		}
	}

}
