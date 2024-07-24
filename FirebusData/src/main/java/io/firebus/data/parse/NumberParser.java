package io.firebus.data.parse;

public class NumberParser extends Parser {

	public static Number parse(String str) {
		if(str.equals("Infinity")) return Double.POSITIVE_INFINITY;
		if(str.equals("-Infinity")) return Double.NEGATIVE_INFINITY;
		if(str.equals("NaN")) return Double.NaN;
		boolean negative = false;
		long integer = 0;
		double decimal = 0;
		
		int l = str.length();
		if(l == 0) return null;
		
		int i = 0;
		char c = str.charAt(i);
		if(c == '-') {
			negative = true;
			i++;
		}
		while(i < l && (c = str.charAt(i++)) != '.') {
			if(!isDigit(c)) return null;
			integer = (10 * integer) + toInt(c);
		}
		if(i == l) {
			if(c != '.') return (negative ? -1L : 1L) * integer;
			else return null;
		} else {
			double div = 10;
			while(i < l) {
				c = str.charAt(i++);
				if(!isDigit(c)) return null;
				decimal = decimal + (toInt(c) / div);
				div *= 10D;
			}
			return (negative ? -1D : 1D) * ((double)integer + decimal);
		}
	}
}
