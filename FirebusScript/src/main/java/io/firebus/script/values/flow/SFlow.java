package io.firebus.script.values.flow;

import io.firebus.script.values.abs.SValue;

public abstract class SFlow extends SValue {

	public boolean equals(SValue other) {
		return false;
	}

	public boolean identical(SValue other) {
		return false;
	}

	public String typeOf() {
		return "statement";
	}
}
