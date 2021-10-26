package io.firebus.script.values.callables.impl.object;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SArray;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SObject;
import io.firebus.script.values.abs.SValue;

public class Entries extends SCallable {

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length >= 1) {
			if(arguments[0] instanceof SObject) {
				SObject o = (SObject)arguments[0];
				String[] keys = o.getMemberKeys();
				SArray array = new SArray();
				for(int i = 0; i < keys.length; i++) {
					SArray subArr = new SArray();
					subArr.add(new SString(keys[i]));
					subArr.add(o.getMember(keys[i]));
					array.set(i, subArr);
				}
				return array;
			} else {
				throw new ScriptCallException("entries requires an object argument");
			}
		} else {
			throw new ScriptCallException("entries requires at least 1 argument");
		}
	}

}
