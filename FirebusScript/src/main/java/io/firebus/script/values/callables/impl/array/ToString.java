package io.firebus.script.values.callables.impl.array;


import java.util.List;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.ArrayFunction;

public class ToString extends ArrayFunction {

	public ToString(List<SValue> a) {
		super(a);
	}

	public SValue call(SValue... arguments) throws ScriptCallException {
		StringBuilder sb = new StringBuilder();
		for(SValue value: values) {
			if(sb.length() > 0)
				sb.append(",");
			sb.append(value.toString());
		}
		return new SString(sb.toString());
	}

}
