package io.firebus.script;

import io.firebus.script.scopes.Scope;

public class Engine {
	
	protected Scope coreScope;
	protected Compiler compiler;
	protected Executer executer;

	public Engine() {
		coreScope = new Scope();
		compiler = new Compiler();
	}
	
	public void compile(String name, String str) {
		compiler.compile(name, str);
	}
	
	public void execute(Scope scope) {
		executer.execute(scope);
	}
}
