package io.firebus.script;

import io.firebus.script.exceptions.ScriptBuildException;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Block;
import io.firebus.script.units.ExecutionUnit;
import io.firebus.script.values.impl.Print;

public class ScriptFactory {
	protected Scope rootScope;
	protected Compiler compiler;
	protected Converter converter;
	
	public ScriptFactory() {
		compiler = new Compiler();
		converter = new Converter();
		rootScope = new Scope();
		rootScope.setValue("print", new Print());
	}
	
	public void executeInRootScope(String name, String source) throws ScriptException {
		executeInRootScope(new Source(name, source));
	}
	
	public void executeInRootScope(Source source) throws ScriptException {
		ExecutionUnit eu = compiler.compile(source);
		eu.eval(rootScope);
	}
	
	public Program createProgram(String source) throws ScriptBuildException {
		return createProgram(new Source("AdhocProgram", source));
	}
	
	public Program createProgram(Source source) throws ScriptBuildException {
		ExecutionUnit eu = compiler.compile(source);
		Program prog = new Program(rootScope, eu);
		return prog;
	}
	
	public Function createFunction(String source) throws ScriptBuildException {
		return createFunction("AdhocFunction", source);
	}
	
	public Function createFunction(String name, String source) throws ScriptBuildException {
		return createFunction(new String[] {}, new Source(name, source));
	}
	
	public Function createFunction(Source source) throws ScriptBuildException {
		return createFunction(new String[] {}, source);
	}
	
	public Function createFunction(String[] params, String source) throws ScriptBuildException {
		return createFunction(params, new Source("AdhocFunction", source));
	}
	
	public Function createFunction(String name, String[] params, String source) throws ScriptBuildException {
		return createFunction(params, new Source(name, source));
	}
	
	public Function createFunction(String[] params, Source source) throws ScriptBuildException {
		ExecutionUnit eu = compiler.compile(source);
		Function func = new Function(rootScope, params, eu, converter);
		return func;
	}
	
	public Expression createExpression(String source) throws ScriptBuildException {
		return createExpression("AdhocExpression", source);
	}
	
	public Expression createExpression(String name, String source) throws ScriptBuildException {
		return createExpression(new Source(name, source));
	}
	
	public Expression createExpression(Source source) throws ScriptBuildException {
		ExecutionUnit eu = compiler.compile(source);
		if(eu instanceof Block) {
			Block block = (Block)eu;
			if(block.getStatementCount() == 1)
				eu = block.getStatement(0);
		}
		Expression expr = new Expression(rootScope, eu, converter);
		return expr;
	}
}
