package io.firebus.script.values.callables.impl.date;


import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.SDate;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.DateFunction;

public class GetHours extends DateFunction {

	public GetHours(SDate d) {
		super(d);
	}

	public SValue call(SValue... arguments) throws ScriptException {
		return new SNumber(date.getZonedDateTime().getHour());
	}

}
