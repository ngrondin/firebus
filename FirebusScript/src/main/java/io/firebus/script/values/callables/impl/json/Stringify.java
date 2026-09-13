package io.firebus.script.values.callables.impl.json;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.script.Converter;
import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SNull;
import io.firebus.script.values.SString;
import io.firebus.script.values.SUndefined;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SObject;
import io.firebus.script.values.abs.SValue;

public class Stringify extends SCallable {

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length >= 1) {
			SValue v = arguments[0];
			if(v instanceof SObject) {
				try {
					Object out = Converter.convertOut(arguments[0]);
					if(out instanceof DataMap) {
						return new SString(((DataMap)out).toString(0, true));
					} else if(out instanceof DataList) {
						return new SString(((DataList)out).toString(0, true));
					} else {
						return new SString(out.toString());
					}
				} catch(Exception e) {
					throw new ScriptCallException("Error converting object to string", e);
				}
			} else if(v instanceof SUndefined) {
				return new SString("");
			} else  if(v instanceof SNull) {
				return new SString("");
			} else {
				throw new ScriptCallException("stringify requires an object argument");
			}
		} else {
			throw new ScriptCallException("stringify requires at least 1 arguments");
		}
	}

}
