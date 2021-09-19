package io.firebus.script.values;

import java.util.Map;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.abs.SPredefinedObject;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.string.CharAt;
import io.firebus.script.values.callables.impl.string.CharCodeAt;
import io.firebus.script.values.callables.impl.string.Concat;
import io.firebus.script.values.callables.impl.string.EndsWith;
import io.firebus.script.values.callables.impl.string.Includes;
import io.firebus.script.values.callables.impl.string.IndexOf;
import io.firebus.script.values.callables.impl.string.Slice;
import io.firebus.script.values.callables.impl.string.StartsWith;
import io.firebus.script.values.callables.impl.string.Substr;

public class SString extends SPredefinedObject {
	protected String str;
	
	public SString(String s) {
		str = s;
	}

	protected Map<String, SValue> defineMembers() {
		return null;
	}

	public String getString() {
		return str;
	}

	public boolean equals(SValue other) {
		return other instanceof SString && str.equals(((SString)other).getString());
	}

	public boolean identical(SValue other) {
		return this == other;
	}
	
	public boolean hasMember(String key) {
		return false;
	}

	public String[] getMemberKeys() {
		return null;
	}

	public SValue getMember(String name) {
		if(name.equals("slice")) {
			return new Slice(this);
		} else if(name.equals("charAt")) {
			return new CharAt(this);
		} else if(name.equals("charCodeAt")) {
			return new CharCodeAt(this);
		} else if(name.equals("concat")) {
			return new Concat(this);
		} else if(name.equals("endsWith")) {
			return new EndsWith(this);
		} else if(name.equals("startsWith")) {
			return new StartsWith(this);
		} else if(name.equals("includes")) {
			return new Includes(this);
		} else if(name.equals("indexOf")) {
			return new IndexOf(this);
		} else if(name.equals("substr")) {
			return new Substr(this);
		}
		return null;
	}

	public String typeOf() {
		return "string";
	}
	
	public String toString() {
		return str;
	}

	public Number toNumber() throws ScriptException {
		try {
			Number number = null;
			if(str.contains(".")) {
				number = Double.parseDouble(str);
			} else {
				number = Long.parseLong(str);
				long l = number.longValue();
				if(l <= 2147483647 && l >= -2147483648) 
					number = (int)l;
			}	
			return number;
		} catch(Exception e) {
			throw new ScriptException("Cannot convert '" + str + "' to number");
		}
	}
	
	public boolean toBoolean() throws ScriptException {
		if(str.equalsIgnoreCase("true"))
			return true;
		else 
			return false;
	}
}
