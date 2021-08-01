package io.firebus.script.units.operators;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.operators.abs.TwoExpressionOperator;
import io.firebus.script.values.SValue;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SString;

public class Add extends TwoExpressionOperator {

	public Add(Expression e1, Expression e2, SourceInfo uc) {
		super(e1, e2, uc);
	}
	

	protected SValue evalWithValues(SValue v1, SValue v2) throws ScriptException {
		if(v1 instanceof SString || v2 instanceof SString) {
			return new SString(v1.toString() + v2.toString());
		} else if(v1 instanceof SNumber && v2 instanceof SNumber) {
			Number n1 = ((SNumber)v1).getNumber();
			Number n2 = ((SNumber)v2).getNumber();
			Number r = null;
			if(n1 instanceof Integer && n2 instanceof Integer) {
				r = n1.intValue() + n2.intValue();
			} else {
				r = n1.doubleValue() + n2.doubleValue();
			}
			return new SNumber(r);
		} else {
			throw new ScriptException("Invalid expressions for add operator", source);
		}
	}
}
