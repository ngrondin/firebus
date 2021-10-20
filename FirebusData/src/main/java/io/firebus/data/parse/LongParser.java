package io.firebus.data.parse;

public class LongParser extends Parser {

	public static Long parse(String str) {
		boolean negative = false;
		long integer = 0;
		
		int l = str.length();
		if(l == 0) return null;
		
		int i = 0;
		char c = str.charAt(i);
		if(c == '-') {
			negative = true;
			i++;
		}	
		while(i < l) {
			c = str.charAt(i++);
			if(!isDigit(c)) return null;
			integer = (10L * integer) + toInt(c);
		}
		return (negative ? -1L : 1L) * integer;
	}
}
