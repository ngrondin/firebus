package io.firebus.script.values.callables.impl.string;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.StringFunction;

public class Substring extends StringFunction {

	public Substring(SString s) {
		super(s);
	}

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length > 0) {
			SValue bv = arguments[0];
			String str = string.getString();
			int len = str.length();
			if(bv instanceof SNumber) {
				int b = ((SNumber)bv).getNumber().intValue();
				if(b < 0) b = 0;
				if(b > len) {
					return new SString("");
				} else {
					if(arguments.length >= 2) {
						SValue ev = arguments[1];
						if(ev instanceof SNumber) {
							int e = ((SNumber)ev).getNumber().intValue();
							if(e > len) e = len;
							if(e <= b) e = b + 1;
							return new SString(str.substring(b, e));
						} else {
							throw new ScriptCallException("substring requires number arguments");
						}
					} else {
						return new SString(string.getString().substring(b));
					}					
				}

			} else {
				throw new ScriptCallException("substring requies number arguments");
			}
		} else {
			throw new ScriptCallException("substring requires at least 1 argument");
		}
	}

}
