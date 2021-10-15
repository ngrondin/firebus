package io.firebus.script.values.callables.impl.string;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SArray;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.StringFunction;

public class Split extends StringFunction {

	public Split(SString s) {
		super(s);
	}

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length > 0) {
			String sep = arguments[0].toString();
			String[] parts = string.getString().split(sep);
			SArray array = new SArray();
			for(int i = 0; i < parts.length; i++)
				array.add(new SString(parts[i]));
			return array;
		} else {
			throw new ScriptCallException("split requires at least 1 argument");
		}
	}

}
