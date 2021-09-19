package io.firebus.script.values.callables.impl.string;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.StringFunction;

public class Substr extends StringFunction {

	public Substr(SString s) {
		super(s);
	}

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length > 0) {
			SValue bv = arguments[0];
			if(bv instanceof SNumber) {
				int b = ((SNumber)bv).getNumber().intValue();
				if(b < 0) b = string.getString().length() + b;
				if(arguments.length >= 2) {
					SValue lv = arguments[1];
					if(lv instanceof SNumber) {
						int l = ((SNumber)lv).getNumber().intValue();
						return new SString(string.getString().substring(b, b + l));
					} else {
						throw new ScriptCallException("substr requies number arguments");
					}
				} else {
					return new SString(string.getString().substring(b));
				}
			} else {
				throw new ScriptCallException("substr requies number arguments");
			}
		} else {
			throw new ScriptCallException("substr requires at least 1 argument");
		}
	}

}
