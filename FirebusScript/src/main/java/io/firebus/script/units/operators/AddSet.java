package io.firebus.script.units.operators;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.operators.abs.Operator;
import io.firebus.script.units.references.Reference;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;

public class AddSet extends Operator {
	
	protected Reference ref;
	protected Expression expression;
	
	public AddSet(Reference r, Expression exp, SourceInfo uc) {
		super(uc);
		ref = r;
		expression = exp;
	}

	public SValue eval(Scope scope) throws ScriptException {
		SValue v1 = ref.eval(scope);
		SValue v2 = expression.eval(scope);
		SValue ret = null;
		if(v1 instanceof SString || v2 instanceof SString) {
			ret = new SString(v1.toString() + v2.toString());
		} else if(v1 instanceof SNumber && v2 instanceof SNumber) {
			Number n1 = ((SNumber)v1).getNumber();
			Number n2 = ((SNumber)v2).getNumber();
			Number r = null;
			if(n1 instanceof Integer && n2 instanceof Integer) {
				r = n1.intValue() + n2.intValue();
			} else {
				r = n1.doubleValue() + n2.doubleValue();
			}
			ret = new SNumber(r);
		} else {
			throw new ScriptException("Invalid expressions for add operator", source);
		}
		
		try {
			ref.setValue(scope, ret);
		} catch(ScriptException e) {
			throw new ScriptException(e.getMessage(), source);
		}
		return ret;
	}

}
