package io.firebus.data.parse;

public class NumberParser extends Parser {

	public static Number parse(String str) {
		long integer = 0;
		double decimal = 0;
		
		int l = str.length();
		if(l == 0) return null;
		
		int i = 0;
		char c = 0;
		while(i < l && (c = str.charAt(i++)) != '.') {
			if(!isDigit(c)) return null;
			integer = (10 * integer) + toInt(c);
		}
		if(i == l) {
			if(c != '.') return integer;
			else return null;
		} else {
			double div = 10;
			while(i < l) {
				c = str.charAt(i++);
				if(!isDigit(c)) return null;
				decimal = decimal + (toInt(c) / div);
				div *= 10D;
			}
			return ((double)integer + decimal);
		}
	}
}
