package io.firebus.script.values.callables;

import java.util.List;

import io.firebus.script.ScriptException;
import io.firebus.script.values.SString;
import io.firebus.script.values.SValue;

public class ArrayJoin extends ArrayFunction {

	public ArrayJoin(List<SValue> v) {
		super(v);
	}

	public SValue call(SValue[] arguments) throws ScriptException {
		String joiner = arguments.length > 0 ? arguments[0].toString() : ",";
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < values.size(); i++) {
			if(i > 0) sb.append(joiner);
			sb.append(values.get(i).toString());
		}
		return new SString(sb.toString());
	}
}
