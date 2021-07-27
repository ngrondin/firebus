package io.firebus.script.units.literals;

import java.util.List;

import io.firebus.script.ScriptException;
import io.firebus.script.scopes.Scope;
import io.firebus.script.units.Literal;
import io.firebus.script.units.PropertySetter;
import io.firebus.script.units.UnitContext;
import io.firebus.script.values.InternalSObject;
import io.firebus.script.values.SValue;

public class ObjectLiteral extends Literal {
	List<PropertySetter> setters;
	
	public ObjectLiteral(List<PropertySetter> s, UnitContext uc) {
		super(uc);
		setters = s;
	}

	public SValue eval(Scope scope) throws ScriptException {
		InternalSObject obj = new InternalSObject();
		for(PropertySetter setter: setters) {
			SValue val = setter.eval(scope);
			obj.setMember(setter.getKey(), val);
		}
		return obj;
	}

}
