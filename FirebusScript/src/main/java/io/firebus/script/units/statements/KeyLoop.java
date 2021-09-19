package io.firebus.script.units.statements;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.Declare;
import io.firebus.script.units.DeclareList;
import io.firebus.script.units.ExecutionUnit;
import io.firebus.script.units.Expression;
import io.firebus.script.units.Statement;
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
					localScope.setValue(declare.getKey(), new SString(keys[index]));
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
			throw new ScriptExecutionException("Expression must be an array", source);
		}
	}
}
