package io.firebus.script.units.operators;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.operators.abs.TwoNumberOperator;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.abs.SValue;

public class RelationalCompare extends TwoNumberOperator {
	protected int type;
	protected final static int GreaterThan = 1;
	protected final static int GreaterOrEqualTo = 2;
	protected final static int LessThan = 3;
	protected final static int LessOrEqualTo = 4;
	
	public RelationalCompare(Expression e1, Expression e2, String compOp, SourceInfo uc) {
		super(e1, e2, uc);
		switch(compOp) {
			case ">" :
				type = GreaterThan;
				break;
			case ">=" :
				type = GreaterOrEqualTo;
				break;
			case "<" :
				type = LessThan;
				break;
			case "<=" :
				type = LessOrEqualTo;
				break;				
		}
	}

	protected SValue evalWithNumbers(Number n1, Number n2) throws ScriptException {
		boolean ret = false;
		switch(type) {
			case GreaterThan :
				ret = n1.doubleValue() > n2.doubleValue();
				break;
			case GreaterOrEqualTo :
				ret = n1.doubleValue() >= n2.doubleValue();
				break;
			case LessThan :
				ret = n1.doubleValue() < n2.doubleValue();
				break;
			case LessOrEqualTo :
				ret = n1.doubleValue() <= n2.doubleValue();
				break;
		}
		return new SBoolean(ret);
	}


}
