package io.firebus.script.values.callables.impl.date;

import java.util.Date;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.SDate;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.DateFunction;

public class SetDate extends DateFunction {

	public SetDate(SDate d) {
		super(d);
	}

	@SuppressWarnings("deprecation")
	public SValue call(SValue... arguments) throws ScriptCallException {
		if(arguments.length > 0) {
			try {
				SValue v = arguments[0];
				int i = v.toNumber().intValue();
				Date dt = date.getDate();
				dt.setDate(i);
				date.setDate(dt);
				return new SNumber(dt.getTime());
			} catch(ScriptValueException e) {
				throw new ScriptCallException("Error in setDate", e);
			}
		} else {
			throw new ScriptCallException("setDate requires at lease 1 argument");
		}
	}

}
