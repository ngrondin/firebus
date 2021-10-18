package io.firebus.script;

import java.util.Map;

import io.firebus.script.exceptions.ScriptBuildException;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.abs.ExecutionUnit;
import io.firebus.script.units.statements.Block;
import io.firebus.script.values.impl.ArrayStaticPackage;
import io.firebus.script.values.impl.DateConstructor;
import io.firebus.script.values.impl.IsNaN;
import io.firebus.script.values.impl.JSONStaticPackage;
import io.firebus.script.values.impl.MathStaticPackage;
import io.firebus.script.values.impl.ObjectStaticPackage;
import io.firebus.script.values.impl.ParseFloat;
import io.firebus.script.values.impl.ParseInt;
import io.firebus.script.values.impl.Print;
import io.firebus.script.values.impl.StringStaticPackage;
import io.firebus.script.values.impl.TimeConstructor;

public class ScriptFactory {
	protected Scope rootScope;
	protected Compiler compiler;
	
	public ScriptFactory() {
		compiler = new Compiler();
		rootScope = new Scope();
		rootScope.declareValue("print", new Print());
		rootScope.declareValue("parseInt", new ParseInt());
		rootScope.declareValue("parseFloat", new ParseFloat());
		rootScope.declareValue("isNaN", new IsNaN());
		rootScope.declareValue("Date", new DateConstructor());
		rootScope.declareValue("Time", new TimeConstructor());
		rootScope.declareValue("Math", new MathStaticPackage());
		rootScope.declareValue("Object", new ObjectStaticPackage());
		rootScope.declareValue("Array", new ArrayStaticPackage());
		rootScope.declareValue("String", new StringStaticPackage());
		rootScope.declareValue("JSON", new JSONStaticPackage());
	}
	
	public void setGlobals(Map<String, Object> globals) throws ScriptException {
		for(String key: globals.keySet()) {
			rootScope.declareValue(key, Converter.convertIn(globals.get(key)));
		}
	}
	
	public void setInRootScope(String name, Object val) throws ScriptException {
		rootScope.declareValue(name, Converter.convertIn(val));
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
	
	public ScriptContext createScriptContext() {
		return new ScriptContext(rootScope);
	}
}
