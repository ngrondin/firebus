package io.firebus.data.parse;

public class DurationParser extends Parser {

	public static Long parse(String str) {
		boolean negative = false;
		long integer = 0;
		long multiplier = 1;
		
		int l = str.length();
		if(l == 0) return null;
		
		int i = 0;
		char c = str.charAt(i);
		if(c == '-') {
			negative = true;
			i++;
		} else if(c == '+') {
			negative = false;
			i++;
		}
		while(i < l - 1) {
			c = str.charAt(i++);
			if(!isDigit(c)) return null;
			integer = (10L * integer) + toInt(c);
		}
		c = str.charAt(i++);
		if(isDigit(c)) {
			integer = (10L * integer) + toInt(c);
		} else if(c == 'd') {
			multiplier = (24*60*60*1000);
		} else if(c == 'h') {
			multiplier = (60*60*1000);
		} else {
			return null;
		}
		return (negative ? -1L : 1L) * integer * multiplier;
	}
}
