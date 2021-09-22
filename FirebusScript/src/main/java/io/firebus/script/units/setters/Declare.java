package io.firebus.script.units.setters;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.VariableId;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.units.abs.Statement;
import io.firebus.script.values.SNull;
import io.firebus.script.values.abs.SValue;

public class Declare extends Statement {
	protected String modifier;
	protected VariableId key;
	protected Expression expression;
	
	public Declare(String k, Expression exp, SourceInfo uc) {
		super(uc);
		key = new VariableId(k);
		expression = exp;
	}
	
	public void setModifier(String m) {
		modifier = m;
	}
	
	public VariableId getKey() {
		return key;
	}

	public SValue eval(Scope scope) throws ScriptExecutionException {
		SValue val = expression != null ? expression.eval(scope) : SNull.get();
		scope.setValue(key, val);
		return val;
	}
}
