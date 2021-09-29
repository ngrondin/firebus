package io.firebus.script;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.firebus.data.DataList;
import io.firebus.data.DataLiteral;
import io.firebus.data.DataMap;
import io.firebus.data.ZonedTime;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.SArray;
import io.firebus.script.values.SBoolean;
import io.firebus.script.values.SDate;
import io.firebus.script.values.SInternalObject;
import io.firebus.script.values.SNull;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.SString;
import io.firebus.script.values.STime;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SObject;
import io.firebus.script.values.abs.SValue;

public class Converter {
	
	public static SValue tryConvertIn(Object o) {
		try {
			return convertIn(o);
		} catch(ScriptException e) {
			return null;
		}
	}

	public static SValue convertIn(Object o) throws ScriptException {
		if(o == null) {
			return SNull.get();
		} else if(o instanceof SValue) {
			return (SValue)o;
		} else if(o instanceof Number) {
			return new SNumber((Number)o);
		} else if(o instanceof String) {
			return new SString((String)o);
		} else if(o instanceof Boolean) {
			return SBoolean.get((Boolean)o);
		} else if(o instanceof Date) {
			return new SDate((Date)o);
		} else if(o instanceof ZonedTime) {
			return new STime((ZonedTime)o);
		} else if(o instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> m = (Map<String, Object>)o;
			SInternalObject io = new SInternalObject();
			for(String key: m.keySet()) 
				io.putMember(key, convertIn(m.get(key)));
			return io;
		} else if(o instanceof DataList) {
			DataList list = (DataList)o;
			SArray a = new SArray();
			for(int i = 0; i < list.size(); i++)
					a.set(i, convertIn(list.get(i)));
			return a;
		} else if(o instanceof List) {
			List<?> list = (List<?>)o;
			SArray a = new SArray();
			for(int i = 0; i < list.size(); i++) 
				a.set(i, convertIn(list.get(i)));
			return a;	
		} else if(o instanceof DataLiteral) {
			DataLiteral dl = (DataLiteral)o;
			return convertIn(dl.getObject());
		} else {
			throw new ScriptValueException("Cannot convert '" + o.toString() + "' to script space");
		}
	}
	
	public static Object tryConvertOut(SValue v) {
		try {
			return convertOut(v);
		} catch(ScriptException e) {
			return null;
		}
	}
	
	public static Object convertOut(SValue v) throws ScriptException {
		if(v instanceof SNull) {
			return null;
		} else if(v instanceof SNumber) {
			Number n = ((SNumber)v).getNumber();
			if(n instanceof Integer)
				return n.intValue();
			else if(n instanceof Long)
				return n.longValue();			
			else if(n instanceof Float)
				return n.floatValue();
			else if(n instanceof Double)
				return n.doubleValue();
			else
				return null;
		} else if(v instanceof SString) {
			return ((SString)v).getString();
		} else if(v instanceof SBoolean) {
			return ((SBoolean)v).getBoolean();
		} else if(v instanceof SDate) {
			return ((SDate)v).getDate();
		} else if(v instanceof STime) {
			return ((STime)v).getTime();
		} else if(v instanceof SArray) {
			SArray a = (SArray)v;
			DataList list = new DataList();
			for(int i = 0; i < a.getSize(); i++)
				list.add(convertOut(a.get(i)));
			return list;
		} else if(v instanceof SObject) {
			SObject o = (SObject)v;
			DataMap map = new DataMap();
			String[] keys = o.getMemberKeys();
			for(int i = 0; i < keys.length; i++) {
				SValue prop = o.getMember(keys[i]);
				if(!(prop instanceof SCallable))
					map.put(keys[i], convertOut(o.getMember(keys[i])));
			}
			return map;
		} else {
			throw new ScriptValueException("Cannot convert '" + v.toString() + "' to java space");
		}
	}
}
