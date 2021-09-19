package io.firebus.script.values.callables.impl.date;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.SDate;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.DateFunction;

public class GetMilliseconds extends DateFunction {

	public GetMilliseconds(SDate d) {
		super(d);
	}

	public SValue call(SValue... arguments) throws ScriptException {
		return new SNumber(date.getZonedDateTime().getNano() / 1000000);
	}

}
