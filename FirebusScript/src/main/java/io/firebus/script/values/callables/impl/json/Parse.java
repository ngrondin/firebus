package io.firebus.script.values.callables.impl.json;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.script.Converter;
import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.SNull;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SString;
import io.firebus.script.values.SUndefined;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;

public class Parse extends SCallable {

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length >= 1) {
			SValue v = arguments[0];
			if(v instanceof SUndefined) {
				throw new ScriptCallException("parse requires a defined argument");
			} else  if(v instanceof SNull || v instanceof SBoolean || v instanceof SNumber) {
				return v;
			} else  if(v instanceof SString) {
				try {
					String str = v.toString();
					if(str.trim().charAt(0) == '{') {
						DataMap map = new DataMap(str);
						return Converter.convertIn(map);						 
					} else if(str.trim().charAt(0) == '[') {
						DataList list = new DataList(str);
						return Converter.convertIn(list);
					} else {
						throw new ScriptCallException("String is neither an object or an array");
					}
				} catch(Exception e) {
					throw new ScriptCallException("Error converting object to string: " +  e.getMessage());
				}
			} else {
				throw new ScriptCallException("Invalid argument type for parse: " + v.typeOf());
			}
		} else {
			throw new ScriptCallException("parse requires at least 1 arguments");
		}
	}
}
