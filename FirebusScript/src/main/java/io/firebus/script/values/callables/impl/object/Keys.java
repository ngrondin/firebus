package io.firebus.script.values.callables.impl.object;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SArray;
import io.firebus.script.values.SString;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SObject;
import io.firebus.script.values.abs.SValue;

public class Keys extends SCallable {

	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length >= 1) {
			if(arguments[0] instanceof SObject) {
				SObject o = (SObject)arguments[0];
				String[] strArr = o.getMemberKeys();
				SArray array = new SArray();
				for(int i = 0; i < strArr.length; i++) 
					array.set(i, new SString(strArr[i]));
				return array;
			} else {
				throw new ScriptCallException("keys requires an object argument");
			}
		} else {
			throw new ScriptCallException("keys requires at least 1 argument");
		}
	}

}
