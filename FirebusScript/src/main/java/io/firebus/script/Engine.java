package io.firebus.script;


import io.firebus.script.scopes.Scope;
import io.firebus.script.units.ExecutionUnit;
import io.firebus.script.values.SCallable;
import io.firebus.script.values.SValue;
import io.firebus.script.values.impl.Print;

public class Engine {
	
	protected Scope coreScope;
	protected Compiler compiler;
	protected Executer executer;

	public Engine() {
		coreScope = new Scope();
		compiler = new Compiler();
		Print p = new Print();
		coreScope.setValue("print", p);

	}
	
	public void compile(String name, String src) throws ScriptException {
		ExecutionUnit root = compiler.compile(src);
		root.eval(coreScope);
	}
	
	public SValue invoke(String name) throws ScriptException {
		return invoke(name, null);
	}
	
	/*public SValue invoke(String name, Object[] rawParams) throws ScriptException {
		List<SValue> params = new ArrayList<SValue>();
		for(int i = 0; i < rawParams.length; i++) {
			Object o = rawParams[i];
			if(o == null) {
				params.add(new SNull());
			} else if(o instanceof SValue) {
				params.add((SValue)o);
			} else if(o instanceof Number) {
				params.add(new SNumber((Number)o));
			} else if(o instanceof String) {
				params.add(new SString((String)o));
			} else if(o instanceof Boolean) {
				params.add(new SBoolean((Boolean)o));
			} 
		}
		return invoke(name, params);
	}*/
	
	public SValue invoke(String name, SValue[] arguments) throws ScriptException {
		SValue c = coreScope.getValue(name);
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
	

}
