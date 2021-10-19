package io.firebus.data.parse;

public class LongParser extends Parser {

	public static Long parse(String str) {
		long n = 0;
		
		int l = str.length();
		if(l == 0) return null;
		
		int i = 0;
		char c = 0;
		while(i < l) {
			c = str.charAt(i++);
			if(!isDigit(c)) return null;
			n = (10L * n) + toInt(c);
		}
		return n;
	}
}
