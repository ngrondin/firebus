package io.firebus.script.values;

import java.util.Map;

import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.abs.SPredefinedObject;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.ToString;
import io.firebus.script.values.callables.impl.string.CharAt;
import io.firebus.script.values.callables.impl.string.CharCodeAt;
import io.firebus.script.values.callables.impl.string.Concat;
import io.firebus.script.values.callables.impl.string.EndsWith;
import io.firebus.script.values.callables.impl.string.Includes;
import io.firebus.script.values.callables.impl.string.IndexOf;
import io.firebus.script.values.callables.impl.string.PadStart;
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
		if(other instanceof SBoolean) {
			Boolean ob = ((SBoolean)other).getBoolean();
			if(ob == false && (str.equals("0") || str.equals("")))
				return true;
			else if(ob == true && str.equals("1"))
				return true;
			else
				return false;
		} if(other instanceof SNumber) {
			Number on = ((SNumber) other).getNumber();
			if(on.longValue() == 0 && str.equals(""))
				return true;
			else 
				return str.equals(on.toString());
		} else {
			String os = other.toString();
			return str.equals(os);
		}
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
		} else if(name.equals("padStart")) {
			return new PadStart(this);
		} else if(name.equals("toString")) {
			return new ToString(this);
		} else if(name.equals("length")) {
			return new SNumber(str.length());
		}
		return SUndefined.get();
	}

	public String typeOf() {
		return "string";
	}
	
	public String toString() {
		return str;
	}

	public Number toNumber() throws ScriptValueException {
		try {
			Number number = null;
			if(str.length() > 0) {
				if(str.contains(".")) {
					number = Double.parseDouble(str);
				} else {
					number = Long.parseLong(str);
				}	
			} else {
				number = 0L;
			}
			return number;
		} catch(Exception e) {
			return Double.NaN;
			//throw new ScriptValueException("Cannot convert '" + str + "' to number");
		}
	}
	
	public Boolean toBoolean() throws ScriptValueException {
		if(str.length() == 0) 
			return false;
		else
			return true;
	}
}
