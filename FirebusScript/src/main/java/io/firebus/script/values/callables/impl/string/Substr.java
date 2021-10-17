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
			String str = string.getString();
			int len = str.length();
			if(bv instanceof SNumber) {
				int b = ((SNumber)bv).getNumber().intValue();
				if(b < 0) b = len + b;
				if(b < 0) b = 0;
				if(b > len) {
					return new SString("");
				} else {
					if(arguments.length >= 2) {
						SValue lv = arguments[1];
						if(lv instanceof SNumber) {
							int l = ((SNumber)lv).getNumber().intValue();
							if(b + l > len) l = len - b;
							return new SString(str.substring(b, b + l));
						} else {
							throw new ScriptCallException("substr requies number arguments");
						}
					} else {
						return new SString(string.getString().substring(b));
					}					
				}

			} else {
				throw new ScriptCallException("substr requies number arguments");
			}
		} else {
			throw new ScriptCallException("substr requires at least 1 argument");
		}
	}

}
