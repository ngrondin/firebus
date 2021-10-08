package io.firebus.script.values.callables.impl.array;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SArray;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.ArrayFunction;

public class Join extends ArrayFunction {

	public Join(SArray a) {
		super(a);
	}

	public SValue call(SValue... arguments) throws ScriptCallException {
		String joiner = arguments.length > 0 ? arguments[0].toString() : ",";
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < array.getSize(); i++) {
			if(i > 0) sb.append(joiner);
			sb.append(array.get(i).toString());
		}
		return new SString(sb.toString());
	}
}
