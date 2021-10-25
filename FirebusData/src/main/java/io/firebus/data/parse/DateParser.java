package io.firebus.data.parse;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class DateParser extends Parser {

	public static ZonedDateTime parse(String str) {
		int year = 0;
		int month = 0;
		int day = 0;
		int hour = 0;
		int minute = 0;
		int second = 0;
		int nano = 0;
		ZoneId zone = null;
		
		int l = str.length();
		if(l < 20) return null;
		
		int i = 0;
		char c = str.charAt(i++);
		if(!isDigit(c)) return null;
		else year += (1000 * toInt(c));

		c = str.charAt(i++);
		if(!isDigit(c)) return null;
		else year += (100 * toInt(c));

		c = str.charAt(i++);
		if(!isDigit(c)) return null;
		else year += (10 * toInt(c));

		c = str.charAt(i++);
		if(!isDigit(c)) return null;
		else year += toInt(c);

		c = str.charAt(i++);
		if(c != '-') return null;
		
		c = str.charAt(i++);
		if(!isDigit(c)) return null;
		else month += (10 * toInt(c));
		
		c = str.charAt(i++);
		if(!isDigit(c)) return null;
		else month += toInt(c);

		c = str.charAt(i++);
		if(c != '-') return null;
		
		c = str.charAt(i++);
		if(!isDigit(c)) return null;
		else day += (10 * toInt(c));
		
		c = str.charAt(i++);
		if(!isDigit(c)) return null;
		else day += toInt(c);

		c = str.charAt(i++);
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
				if(isDigit(c)) {
					nano += (100 * toInt(c));

					c = str.charAt(i++);
					if(!isDigit(c)) return null;
					else nano += (10 * toInt(c));

					c = str.charAt(i++);
					if(!isDigit(c)) return null;
					else nano += toInt(c);
					
					c = str.charAt(i++);
				}
			}
		}
		if(c != '+' && c != '-' && c != 'Z') return null;
		if(c == 'Z') {
			zone = ZoneId.of("Z");
		} else if(c == '+' || c == '-') {
			int offsetHour = 0;
			int offsetMinute = 0;
			
			c = str.charAt(i++);
			if(!isDigit(c)) return null;
			else offsetHour += (10 * toInt(c));
			
			c = str.charAt(i++);
			if(!isDigit(c)) return null;
			else offsetHour += toInt(c);
			
			c = str.charAt(i++);
			if(c != ':') return null;

			c = str.charAt(i++);
			if(!isDigit(c)) return null;
			else offsetMinute += (10 * toInt(c));
			
			c = str.charAt(i++);
			if(!isDigit(c)) return null;
			else offsetMinute += toInt(c);
			
			zone = ZoneOffset.ofHoursMinutes(offsetHour, offsetMinute);
		}
		
		if(i < l) return null;
		
		return ZonedDateTime.of(year, month, day, hour, minute, second, nano, zone);
	}
}
