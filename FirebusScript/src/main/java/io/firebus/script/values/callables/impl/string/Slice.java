package io.firebus.script.values.callables.impl.string;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.StringFunction;

public class Slice extends StringFunction {

	public Slice(SString s) {
		super(s);
	}

	public SValue call(SValue... arguments) throws ScriptException {
		if(arguments.length > 0) {
			SValue bv = arguments[0];
			if(bv instanceof SNumber) {
				int b = ((SNumber)bv).getNumber().intValue();
				if(b < 0) b = string.getString().length() + b;
				if(arguments.length >= 2) {
					SValue ev = arguments[1];
					if(ev instanceof SNumber) {
						int e = ((SNumber)ev).getNumber().intValue();
						if(e < 0) e = string.getString().length() + e;
						return new SString(string.getString().substring(b, e));
					} else {
						throw new ScriptException("slice requies number arguments");
					}
				} else {
					return new SString(string.getString().substring(b));
				}
			} else {
				throw new ScriptException("slice requies number arguments");
			}
		} else {
			throw new ScriptException("slice requires at least 1 argument");
		}
	}

}
