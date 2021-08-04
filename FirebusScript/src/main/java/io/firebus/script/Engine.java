package io.firebus.script;


import java.util.Map;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.ExecutionUnit;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.SNull;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.impl.Print;

public class Engine {
	
	protected Scope rootScope;
	protected Compiler compiler;
	protected Executer executer;

	public Engine() {
		rootScope = new Scope();
		compiler = new Compiler();
		Print p = new Print();
		rootScope.setValue("print", p);

	}
	
	public ExecutionUnit compile(String src) throws ScriptException {
		return compiler.compile(src);
	}
	
	public void eval(String src) throws ScriptException {
		eval(compile(src));
	}
	
	public void eval(ExecutionUnit unit) throws ScriptException {
		unit.eval(rootScope);
	}
	
	public SValue invoke(String name) throws ScriptException {
		return invoke(name, null);
	}
	
	public SValue invoke(String name, Object[] arguments) throws ScriptException {
		SValue[] args = new SValue[arguments.length];
		for(int i = 0; i < arguments.length; i++)
			args[i] = convertIn(arguments[i]);
		return invoke(name, args);
	}
	
	
	public SValue invoke(String name, SValue[] arguments) throws ScriptException {
		SValue c = rootScope.getValue(name);
		if(c != null) {
			if(c instanceof SCallable) {
				SValue ret = ((SCallable)c).call(arguments);
				return ret;
			} else {
				throw new ScriptException("'" + name + "' is not callable", null);
			}
		} else {
			throw new ScriptException("Function does not exist", null);
		}
	}
	
	
	public Scope createScope(Map<String, Object> map) {
		Scope scope = new Scope(rootScope);
		for(String key: map.keySet()) {
			scope.setValue(key, convertIn(map.get(key)));
		}
		return scope;
	}
	
	protected SValue convertIn(Object o) {
		if(o == null) {
			return new SNull();
		} else if(o instanceof SValue) {
			return (SValue)o;
		} else if(o instanceof Number) {
			return new SNumber((Number)o);
		} else if(o instanceof String) {
			return new SString((String)o);
		} else if(o instanceof Boolean) {
			return new SBoolean((Boolean)o);
		} 		
		return null;
	}
	
	protected Object convertOut(SValue v) {
		return null;
	}

}
