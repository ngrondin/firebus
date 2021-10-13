package io.firebus.script.tools;

import io.firebus.script.units.abs.Literal;

public class Parameter {
	public String name;
	public Literal defaultLiteral;
	
	public Parameter(String p, Literal l) {
		name = p;
		defaultLiteral = l;
	}

	public String toString() {
		return name + (defaultLiteral != null ? " = " + defaultLiteral.toString() : "");
	}
}
