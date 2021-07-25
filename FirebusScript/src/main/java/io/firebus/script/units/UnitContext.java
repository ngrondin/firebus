package io.firebus.script.units;

public class UnitContext {
	protected String name;
	protected int line;
	protected int column;
	
	public UnitContext(String n, int l, int c) {
		name = n;
		line = l;
		column = c;
	}
	
	public String toString() {
		return line + ":" + column;
	}
}
