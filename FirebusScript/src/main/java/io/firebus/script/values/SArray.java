package io.firebus.script.values;

import java.util.ArrayList;
import java.util.List;

import io.firebus.script.exceptions.ScriptValueException;
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
import io.firebus.script.values.callables.impl.array.ToString;

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
	
	public List<SValue> getValues() {
		return values;
	}
	
	public SValue remove(int i) {
		SValue ret = values.remove(i);
		return ret;
	}
	
	public void add(SValue v) {
		values.add(v);
	}
	
	public void set(int i, SValue v)  {
		if(i == values.size()) {
			values.add(v);
		} else if(i < values.size() && i >= 0) {
			values.remove(i);
			values.add(i, v);
		} else if(i > values.size()){
			for(int j = values.size(); j < i; j++)
				values.add(SNull.get());
			values.add(v);
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
				return new Map(this);
			} else if(key.equals("join")) {
				return new Join(this);
			} else if(key.equals("concat")) {
				return new Concat(this);
			} else if(key.equals("filter")) {
				return new Filter(this);
			} else if(key.equals("find")) {
				return new Find(this);
			} else if(key.equals("forEach")) {
				return new ForEach(this);
			} else if(key.equals("includes")) {
				return new Includes(this);
			} else if(key.equals("indexOf")) {
				return new IndexOf(this);
			} else if(key.equals("lastIndexOf")) {
				return new LastIndexOf(this);
			} else if(key.equals("of")) {
				return new Of(this);
			} else if(key.equals("push")) {
				return new Push(this);
			} else if(key.equals("pop")) {
				return new Pop(this);
			} else if(key.equals("reduce")) {
				return new Reduce(this);
			} else if(key.equals("slice")) {
				return new Slice(this);
			} else if(key.equals("sort")) {
				return new Sort(this);
			} else if(key.equals("splice")) {
				return new Splice(this);
			} else if(key.contentEquals("length")) {
				return new SNumber(values.size());
			} else if(key.contentEquals("toString")) {
				return new ToString(this);
			}
		} 
		return SUndefined.get();
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
	
	public Number toNumber() throws ScriptValueException {
		throw new ScriptValueException("Array cannot be converted to number");
	}
	
	public Boolean toBoolean() throws ScriptValueException {
		throw new ScriptValueException("Array cannot be converted to boolean");
	}
	
}
