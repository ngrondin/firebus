package io.firebus.script.values;

import java.util.ArrayList;
import java.util.List;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.abs.SPredefinedObject;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.array.Concat;
import io.firebus.script.values.callables.impl.array.Filter;
import io.firebus.script.values.callables.impl.array.Find;
import io.firebus.script.values.callables.impl.array.ForEach;
import io.firebus.script.values.callables.impl.array.Includes;
import io.firebus.script.values.callables.impl.array.IndexOf;
import io.firebus.script.values.callables.impl.array.Join;
import io.firebus.script.values.callables.impl.array.LastIndexOf;
import io.firebus.script.values.callables.impl.array.Map;
import io.firebus.script.values.callables.impl.array.Of;
import io.firebus.script.values.callables.impl.array.Pop;
import io.firebus.script.values.callables.impl.array.Push;
import io.firebus.script.values.callables.impl.array.Reduce;
import io.firebus.script.values.callables.impl.array.Slice;
import io.firebus.script.values.callables.impl.array.Sort;
import io.firebus.script.values.callables.impl.array.Splice;

public class SArray extends SPredefinedObject {
	protected List<SValue> values;
	protected static String[] keys = {"length", "map", "join", "concat", "filter", "find", "forEach", "includes", "indexOf", "lastIndexOf", "of", "push", "pop", "reduce", "slice", "sort", "splice"};
	
	public SArray() {
		super();
		values = new ArrayList<SValue>();
	}
	
	public SArray(List<SValue> l) {
		super();
		values = l;
	}
	
	public SValue get(int i) {
		return values.get(i);
	}
	
	public int getSize() {
		return values.size();
	}
	
	public void remove(int i) {
		
	}
	
	public void set(int i, SValue v) throws ScriptException {
		if(i == values.size()) {
			values.add(v);
		} else if(i < values.size()) {
			values.remove(i);
			values.add(i, v);
		} else {
			throw new ScriptException("Array out of bound", null);
		}
	}
	
	public String[] getMemberKeys() {
		return keys;
	}

	public SValue getMember(String key) {
		if(key != null) {
			if(key.equals("length")) {
				return new SNumber(values.size());
			} else if(key.equals("map")) {
				return new Map(values);
			} else if(key.equals("join")) {
				return new Join(values);
			} else if(key.equals("concat")) {
				return new Concat(values);
			} else if(key.equals("filter")) {
				return new Filter(values);
			} else if(key.equals("find")) {
				return new Find(values);
			} else if(key.equals("forEach")) {
				return new ForEach(values);
			} else if(key.equals("includes")) {
				return new Includes(values);
			} else if(key.equals("indexOf")) {
				return new IndexOf(values);
			} else if(key.equals("lastIndexOf")) {
				return new LastIndexOf(values);
			} else if(key.equals("of")) {
				return new Of(values);
			} else if(key.equals("push")) {
				return new Push(values);
			} else if(key.equals("pop")) {
				return new Pop(values);
			} else if(key.equals("reduce")) {
				return new Reduce(values);
			} else if(key.equals("slice")) {
				return new Slice(values);
			} else if(key.equals("sort")) {
				return new Sort(values);
			} else if(key.equals("splice")) {
				return new Splice(values);
			}
		} 
		return null;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(SValue val: values) {
			if(sb.length() > 1) 
				sb.append(", ");
			if(val instanceof SString) {
				sb.append("\"" + val + "\"");
			} else {
				sb.append(val);
			}
		}
		sb.append("]");
		return sb.toString();
	}
}
