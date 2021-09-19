package io.firebus.script;

import java.util.Map;

import io.firebus.script.exceptions.ScriptBuildException;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Block;
import io.firebus.script.units.ExecutionUnit;
import io.firebus.script.values.impl.DateConstructor;
import io.firebus.script.values.impl.Math;
import io.firebus.script.values.impl.ParseInt;
import io.firebus.script.values.impl.Print;
import io.firebus.script.values.impl.TimeConstructor;

public class ScriptFactory {
	protected Scope rootScope;
	protected Compiler compiler;
	
	public ScriptFactory() {
		compiler = new Compiler();
		rootScope = new Scope();
		rootScope.setValue("print", new Print());
		rootScope.setValue("parseInt", new ParseInt());
		rootScope.setValue("Date", new DateConstructor());
		rootScope.setValue("Time", new TimeConstructor());
		rootScope.setValue("Math", new Math());
	}
	
	public void setGlobals(Map<String, Object> globals) throws ScriptException {
		for(String key: globals.keySet()) {
			rootScope.setValue(key, Converter.convertIn(globals.get(key)));
		}
	}
	
	public void setInRootScope(String name, Object val) throws ScriptException {
		rootScope.setValue(name, Converter.convertIn(val));
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
		Function func = new Function(rootScope, params, eu);
		return func;
	}
	
	public Expression createExpression(String source) throws ScriptBuildException {
		return createExpression("AdhocExpression", source);
	}
	
	public Expression createExpression(String name, String source) throws ScriptBuildException {
		return createExpression(new ExpressionSource(name, source));
	}
	
	public Expression createExpression(ExpressionSource source) throws ScriptBuildException {
		ExecutionUnit eu = compiler.compile(source);
		if(eu instanceof Block) {
			Block block = (Block)eu;
			if(block.getStatementCount() == 1)
				eu = block.getStatement(0);
		}
		Expression expr = new Expression(rootScope, eu);
		return expr;
	}
}
