package io.firebus.script;

import io.firebus.script.scopes.Scope;
import io.firebus.script.units.ExecutionUnit;
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
	

}
