package io.firebus.script;

import io.firebus.script.exceptions.ScriptException;

public class ScriptContext {
	protected Scope scope;
	
	protected ScriptContext(Scope parent) {
		scope = new Scope(parent);		
	}
	
	public void put(String name, Object obj) throws ScriptException {
		scope.setValue(name, Converter.convertIn(obj));
	}
	
	public Scope getScope() {
		return scope;
	}
}
