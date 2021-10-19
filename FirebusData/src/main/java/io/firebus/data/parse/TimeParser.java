package io.firebus.data.parse;

import io.firebus.data.ZonedTime;

public class TimeParser extends Parser {

	public static ZonedTime parse(String str) {
		int hour = 0;
		int minute = 0;
		int second = 0;
		int nano = 0;
		String zoneName = null;
		
		int l = str.length();
		if(l < 11) return null;
		int i = 0;
		char c = str.charAt(i++);
		if(c != 'T') return null;

		c = str.charAt(i++);
		if(!isDigit(c)) return null;
		else hour += (10 * toInt(c));
		
		c = str.charAt(i++);
		if(!isDigit(c)) return null;
		else hour += toInt(c);

		c = str.charAt(i++);
		if(c != ':') return null;

		c = str.charAt(i++);
		if(!isDigit(c)) return null;
		else minute += (10 * toInt(c));
		
		c = str.charAt(i++);
		if(!isDigit(c)) return null;
		else minute += toInt(c);
		
		c = str.charAt(i++);
		if(c != ':') return null;

		c = str.charAt(i++);
		if(!isDigit(c)) return null;
		else second += (10 * toInt(c));
		
		c = str.charAt(i++);
		if(!isDigit(c)) return null;
		else second += toInt(c);

		c = str.charAt(i++);
		if(c == '.') {
			
			c = str.charAt(i++);
			if(!isDigit(c)) return null;
			else nano += (100000000 * toInt(c));

			c = str.charAt(i++);
			if(!isDigit(c)) return null;
			else nano += (10000000 * toInt(c));

			c = str.charAt(i++);
			if(!isDigit(c)) return null;
			else nano += (1000000 * toInt(c));

			c = str.charAt(i++);
			if(isDigit(c)) {
				nano += (100000 * toInt(c));

				c = str.charAt(i++);
				if(!isDigit(c)) return null;
				else nano += (10000 * toInt(c));

				c = str.charAt(i++);
				if(!isDigit(c)) return null;
				else nano += (1000 * toInt(c));
				
				c = str.charAt(i++);
			}
		}
		if(c != '[') return null;
		StringBuilder sb = new StringBuilder();
		while(i < l && (c = str.charAt(i++)) != ']')
			sb.append(c);
		
		if(c != ']') return null;
		zoneName = sb.toString();
		
		if(i < l) return null;
		
		return new ZonedTime(hour, minute, second, nano, zoneName);
	}
}
