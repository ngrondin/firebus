package io.firebus.script.values.callables.impl.date;

import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.values.SDate;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.DateFunction;

public class GetDay extends DateFunction {

	public GetDay(SDate zdt) {
		super(zdt);
	}

	public SValue call(SValue... arguments) throws ScriptCallException {
		return new SNumber(date.getZonedDateTime().getDayOfWeek().getValue() % 7);
	}

}
