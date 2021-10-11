package io.firebus.script.values.callables.impl.json;

import io.firebus.data.DataMap;
import io.firebus.script.Converter;
import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SInternalObject;
import io.firebus.script.values.SNull;
import io.firebus.script.values.SUndefined;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;

public class Parse extends SCallable {

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length >= 1) {
			SValue v = arguments[0];
			if(v instanceof SUndefined) {
				throw new ScriptCallException("parse requires a defined argument");
			} else  if(v instanceof SNull || v instanceof SInternalObject) {
				return v;
			} else {
				try {
					String str = v.toString();
					DataMap map = new DataMap(str);
					return Converter.convertIn(map);
				} catch(Exception e) {
					throw new ScriptCallException("Error converting object to string", e);
				}
			}
		} else {
			throw new ScriptCallException("parse requires at least 1 arguments");
		}
	}
}
