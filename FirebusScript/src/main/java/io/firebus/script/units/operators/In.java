package io.firebus.script.units.operators;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.units.operators.abs.TwoExpressionOperator;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.abs.SObject;
import io.firebus.script.values.abs.SValue;

public class In extends TwoExpressionOperator {

	public In(Expression e1, Expression e2, SourceInfo uc) {
		super(e1, e2, uc);
	}

	protected SValue evalWithValues(SValue v1, SValue v2) throws ScriptExecutionException, ScriptValueException {
		if(v2 instanceof SObject) {
			String propName = v1.toString();
			SObject o = (SObject)v2;
			if(o.hasMember(propName)) {
				return SBoolean.get(true);
			} else {
				return SBoolean.get(false);
			}
		} else {
			throw new ScriptExecutionException("'in' operator requires an object", source);
		}
	}

}
