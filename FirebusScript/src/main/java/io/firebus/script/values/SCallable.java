package io.firebus.script.values;

import java.util.List;

import io.firebus.script.ScriptException;

public abstract class SCallable extends SValue {

	public abstract SValue call(List<SValue> params) throws ScriptException;
	

	public boolean equals(SValue other) {
		return this == other;
	}

	public boolean identical(SValue other) {
		return this == other;
	}
}
