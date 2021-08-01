package io.firebus.script.units.operators;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.operators.abs.TwoExpressionOperator;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.SValue;

public class EqualityCompare extends TwoExpressionOperator {
	protected int type;
	protected final static int Equals = 1;
	protected final static int NotEquals = 2;
	protected final static int Identical = 3;
	protected final static int NotIdentical = 4;
	
	public EqualityCompare(Expression e1, Expression e2, String compOp, SourceInfo uc) {
		super(e1, e2, uc);
		switch(compOp) {
			case "==" :
				type = Equals;
				break;
			case "!=" :
				type = NotEquals;
				break;
			case "===" :
				type = Identical;
				break;
			case "!==" :
				type = NotIdentical;
				break;			
		}
	}

	protected SValue evalWithValues(SValue v1, SValue v2) throws ScriptException {
		boolean ret = false;
		switch(type) {
			case Equals :
				ret = v1.equals(v2);
				break;
			case NotEquals :
				ret = !v1.equals(v2);
				break;
			case Identical :
				ret = v1.identical(v2);
				break;
			case NotIdentical :
				ret = !v1.identical(v2);
				break;
		}		
		return new SBoolean(ret);
	}


}
