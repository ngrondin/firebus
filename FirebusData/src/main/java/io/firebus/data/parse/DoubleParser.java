package io.firebus.data.parse;

public class DoubleParser extends Parser {

	public static Double parse(String str) {
		double d = 0;
		
		int l = str.length();
		if(l < 3) return null;
		int i = 0;
		char c = 0;
		while(i < l && (c = str.charAt(i++)) != '.') {
			if(!isDigit(c)) return null;
			d = (10D * d) + toInt(c);
		}
		if(i == l) return null;
		double div = 10;
		while(i < l) {
			c = str.charAt(i++);
			if(!isDigit(c)) return null;
			d = d + (toInt(c) / div);
			div *= 10D;
		}
		
		return d;
	}
}
