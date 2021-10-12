package io.firebus.script.units.statements;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.abs.ExecutionUnit;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.units.abs.Statement;
import io.firebus.script.units.setters.Declare;
import io.firebus.script.units.setters.DeclareList;
import io.firebus.script.values.SNull;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SObject;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.flow.SBreak;
import io.firebus.script.values.flow.SReturn;

public class KeyLoop extends Statement {
	protected DeclareList declares;
	protected Expression objectExpr;
	protected ExecutionUnit unit;

	public KeyLoop(DeclareList d, Expression o, ExecutionUnit eu, SourceInfo uc) {
		super(uc);
		declares = d;
		objectExpr = o;
		unit = eu;
	}
	
	public SValue eval(Scope scope) throws ScriptExecutionException {
		SValue o = objectExpr.eval(scope);
		if(o instanceof SObject) {
			Scope localScope = new Scope(scope);
			SObject object = (SObject)o;
			String[] keys = object.getMemberKeys();
			int index = 0;
			while(index < keys.length) {
				for(Declare declare: declares.getDeclares())
					localScope.declareValue(declare.getKey(), new SString(keys[index]));
				SValue ret = unit.eval(localScope);
				if(ret instanceof SReturn) {
					return ret;
				} else if(ret instanceof SBreak) {
					return SNull.get();
				}	
				index++;
			}
			return SNull.get();			
		} else {
			throw new ScriptExecutionException("Expression must be an array", source);
		}
	}
}
