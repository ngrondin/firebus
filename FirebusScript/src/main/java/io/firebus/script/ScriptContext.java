package io.firebus.script;

import io.firebus.script.exceptions.ScriptValueException;

public class ScriptContext {
	protected Scope scope;
	
	protected ScriptContext(Scope parent) {
		scope = new Scope(parent);		
	}
	
	public ScriptContext createChild() {
		return new ScriptContext(scope);
	}
	
	public void put(String name, Object obj) throws ScriptValueException {
		scope.setValue(name, Converter.convertIn(obj));
	}
	
	public void remove(String name) {
		scope.removeValue(name);
	}
	
	public Scope getScope() {
		return scope;
	}
}
