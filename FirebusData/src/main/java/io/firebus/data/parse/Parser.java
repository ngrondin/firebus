package io.firebus.data.parse;

public abstract class Parser {

	
	protected static boolean isDigit(char c) {
		int i = (int)c;
		return i >= 48 && i <=57;
	}
	
	protected static boolean isAlpha(char c) {
		int i = (int)c;
		return (i >= 65 && i <= 90) || (i >= 97 && i <= 122);
	}
	
	protected static int toInt(char c) {
		return ((int)c) - 48;
	}
	
	
}
