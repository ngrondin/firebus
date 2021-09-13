package io.firebus.script.values.abs;

public abstract class SValue {

	public abstract boolean equals(SValue other);
	
	public abstract boolean identical(SValue other);
	
	public abstract String typeOf();
	
	public String asString() {
		return toString();
	}
}
