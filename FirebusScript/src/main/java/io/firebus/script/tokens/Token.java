package io.firebus.script.tokens;

public abstract class Token {
	protected String sourceName;
	protected int line;
	protected int column;

	public Token(String sn, int l, int c) {
		sourceName = sn;
		line = l;
		column = c;
	}
	
	public String getSourceFileName() {
		return sourceName;
	}
	
	public int getLine() {
		return line;
	}
	
	public int getColumn() {
		return column;
	}
	
	public abstract boolean is(String str);

}
