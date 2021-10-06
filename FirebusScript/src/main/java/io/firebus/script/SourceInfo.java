package io.firebus.script;

public class SourceInfo {
	protected String sourceName;
	protected String text;
	protected int line;
	protected int column;
	
	public SourceInfo(String sn, String t, int l, int c) {
		sourceName = sn;
		text = t;
		line = l;
		column = c;
	}
	
	public String getSourceName() {
		return sourceName;
	}
	
	public int getLine() {
		return line;
	}
	
	public int getColumn() {
		return column;
	}
	
	public String toString() {
		return text;
	}
	
	public String getLineCol() {
		return (sourceName != null ? sourceName + " " : "") + line + ":" + column;
	}
}
