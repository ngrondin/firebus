package io.firebus.script.units.statements;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.units.Declare;
import io.firebus.script.units.DeclareList;
import io.firebus.script.units.ExecutionUnit;
import io.firebus.script.units.Expression;
import io.firebus.script.units.Statement;
import io.firebus.script.values.SArray;
import io.firebus.script.values.SNull;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.flow.SBreak;
import io.firebus.script.values.flow.SReturn;

public class ArrayLoop extends Statement {
	protected DeclareList declares;
	protected Expression arrayExpr;
	protected ExecutionUnit unit;

	public ArrayLoop(DeclareList d, Expression a, ExecutionUnit eu, SourceInfo uc) {
		super(uc);
		declares = d;
		arrayExpr = a;
		unit = eu;
	}
	
	public SValue eval(Scope scope) throws ScriptException {
		SValue a = arrayExpr.eval(scope);
		if(a instanceof SArray) {
			Scope localScope = new Scope(scope);
			SArray array = (SArray)a;
			int index = 0;
			while(index < array.getSize()) {
				for(Declare declare: declares.getDeclares())
					localScope.setValue(declare.getKey(), array.get(index));
				SValue ret = unit.eval(localScope);
				if(ret instanceof SReturn) {
					return ret;
				} else if(ret instanceof SBreak) {
					return new SNull();
				}
				index++;
			}
			return new SNull();			
		} else {
			throw new ScriptException("Expression must be an array", source);
		}
	}
}