package io.firebus.script.units.operators;

import io.firebus.script.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.UnitContext;
import io.firebus.script.units.operators.abs.TwoNumberOperator;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SValue;

public class Substract extends TwoNumberOperator {

	public Substract(Expression e1, Expression e2, UnitContext uc) {
		super(e1, e2, uc);
	}

	protected SValue evalWithNumbers(Number n1, Number n2) throws ScriptException {
		Number r = null;
		if(n1 instanceof Integer && n2 instanceof Integer) {
			r = n1.intValue() - n2.intValue();
		} else {
			r = n1.doubleValue() - n2.doubleValue();
		}
		return new SNumber(r);
	}
}
