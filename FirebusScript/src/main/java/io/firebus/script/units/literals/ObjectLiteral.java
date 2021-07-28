package io.firebus.script.units.literals;

import java.util.ArrayList;
import java.util.List;

import io.firebus.script.ScriptException;
import io.firebus.script.SourceInfo;
import io.firebus.script.scopes.Scope;
import io.firebus.script.units.Expression;
import io.firebus.script.units.Literal;
import io.firebus.script.values.InternalSObject;
import io.firebus.script.values.SValue;

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

	public SValue eval(Scope scope) throws ScriptException {
		InternalSObject obj = new InternalSObject();
		for(Setter setter: setters) {
			SValue val = setter.expr.eval(scope);
			obj.putMember(setter.key, val);
		}
		return obj;
	}

}
