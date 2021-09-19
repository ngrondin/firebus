package io.firebus.script.units.literals;

import java.util.ArrayList;
import java.util.List;

import io.firebus.script.Scope;
import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptExecutionException;
import io.firebus.script.units.Expression;
import io.firebus.script.units.Literal;
import io.firebus.script.values.SInternalObject;
import io.firebus.script.values.abs.SValue;

public class ObjectLiteral extends Literal {
	class Setter {
		public String key;
		public Expression expr;
	}
	
	List<Setter> setters;
	
	public ObjectLiteral(SourceInfo uc) {
		super(uc);
		setters = new ArrayList<Setter>();
	}
	
	public void addSetter(String k, Expression e) {
		Setter setter = new Setter();
		setter.key = k;
		setter.expr = e;
		setters.add(setter);
	}

	public SValue eval(Scope scope) throws ScriptExecutionException {
		SInternalObject obj = new SInternalObject();
		Scope local = new Scope(scope);
		local.setValue("this", obj);
		for(Setter setter: setters) {
			SValue val = setter.expr.eval(local);
			obj.putMember(setter.key, val);
		}
		return obj;
	}

}
