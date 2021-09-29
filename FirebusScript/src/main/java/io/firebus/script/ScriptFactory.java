package io.firebus.script;

import java.util.Map;

import io.firebus.script.exceptions.ScriptBuildException;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.abs.ExecutionUnit;
import io.firebus.script.units.statements.Block;
import io.firebus.script.values.impl.ArrayStaticPackage;
import io.firebus.script.values.impl.DateConstructor;
import io.firebus.script.values.impl.JSONStaticPackage;
import io.firebus.script.values.impl.MathStaticPackage;
import io.firebus.script.values.impl.ObjectStaticPackage;
import io.firebus.script.values.impl.ParseInt;
import io.firebus.script.values.impl.Print;
import io.firebus.script.values.impl.TimeConstructor;

public class ScriptFactory {
	protected Scope rootScope;
	protected Compiler compiler;
	
	public ScriptFactory() {
		compiler = new Compiler();
		rootScope = new Scope();
		rootScope.setValue(new VariableId("print"), new Print());
		rootScope.setValue(new VariableId("parseInt"), new ParseInt());
		rootScope.setValue(new VariableId("Date"), new DateConstructor());
		rootScope.setValue(new VariableId("Time"), new TimeConstructor());
		rootScope.setValue(new VariableId("Math"), new MathStaticPackage());
		rootScope.setValue(new VariableId("Object"), new ObjectStaticPackage());
		rootScope.setValue(new VariableId("Array"), new ArrayStaticPackage());
		rootScope.setValue(new VariableId("JSON"), new JSONStaticPackage());
	}
	
	public void setGlobals(Map<String, Object> globals) throws ScriptException {
		for(String key: globals.keySet()) {
			rootScope.setValue(new VariableId(key), Converter.convertIn(globals.get(key)));
		}
	}
	
	public void setInRootScope(String name, Object val) throws ScriptException {
		rootScope.setValue(new VariableId(name), Converter.convertIn(val));
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
		if(source.isFixedValue()) {
			return new FixedExpression(source.getFixedValue());
		} else {
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
}
