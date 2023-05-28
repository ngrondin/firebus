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
		if(l < 19 || l > 35) return null;
		
		int i = 0;
		char c = str.charAt(i);
		if(!isDigit(c)) return null;
		else year += (1000 * toInt(c));

		c = str.charAt(++i);
		if(!isDigit(c)) return null;
		else year += (100 * toInt(c));

		c = str.charAt(++i);
		if(!isDigit(c)) return null;
		else year += (10 * toInt(c));

		c = str.charAt(++i);
		if(!isDigit(c)) return null;
		else year += toInt(c);

		c = str.charAt(++i);
		if(c != '-') return null;
		
		c = str.charAt(++i);
		if(!isDigit(c)) return null;
		else month += (10 * toInt(c));
		
		c = str.charAt(++i);
		if(!isDigit(c)) return null;
		else month += toInt(c);

		c = str.charAt(++i);
		if(c != '-') return null;
		
		c = str.charAt(++i);
		if(!isDigit(c)) return null;
		else day += (10 * toInt(c));
		
		c = str.charAt(++i);
		if(!isDigit(c)) return null;
		else day += toInt(c);

		c = str.charAt(++i);
		if(c != 'T') return null;

		c = str.charAt(++i);
		if(!isDigit(c)) return null;
		else hour += (10 * toInt(c));
		
		c = str.charAt(++i);
		if(!isDigit(c)) return null;
		else hour += toInt(c);

		c = str.charAt(++i);
		if(c != ':') return null;

		c = str.charAt(++i);
		if(!isDigit(c)) return null;
		else minute += (10 * toInt(c));
		
		c = str.charAt(++i);
		if(!isDigit(c)) return null;
		else minute += toInt(c);
		
		c = str.charAt(++i);
		if(c != ':') return null;

		c = str.charAt(++i);
		if(!isDigit(c)) return null;
		else second += (10 * toInt(c));
		
		c = str.charAt(++i);
		if(!isDigit(c)) return null;
		else second += toInt(c);

		if(i == l - 1) {
			zone = ZoneId.of("Z");
		} else {
			c = str.charAt(++i);
			if(c == '.') {
				long subsec = 0;
				long subsecdiv = 1;
				while(true) {
					i++;
					if(i < l) {
						c = str.charAt(i);
						if(isDigit(c)) {
							subsec = (10 * subsec) + toInt(c);
							subsecdiv *= 10;;
						} else {
							break;
						}						
					} else {
						c = 0;
						break;
					}
				}
				nano = (int)(1000000000L * subsec / subsecdiv);
			}
			if(i == l) {
				zone = ZoneId.of("Z");
			} else {
				if(c == 'Z') {
					zone = ZoneId.of("Z");
				} else if(c == '+' || c == '-') {
					int offsetSign = c == '+' ? 1 : -1;
					int offsetHour = 0;
					int offsetMinute = 0;
					
					c = str.charAt(++i);
					if(!isDigit(c)) return null;
					else offsetHour += (10 * toInt(c));
					
					c = str.charAt(++i);
					if(!isDigit(c)) return null;
					else offsetHour += toInt(c);
					
					if(i < l - 1) {
						c = str.charAt(++i);
						if(c != ':') return null;

						c = str.charAt(++i);
						if(!isDigit(c)) return null;
						else offsetMinute += (10 * toInt(c));
						
						c = str.charAt(++i);
						if(!isDigit(c)) return null;
						else offsetMinute += toInt(c);						
					}
					
					zone = ZoneOffset.ofHoursMinutes(offsetSign * 	offsetHour, offsetMinute);
				} else {
					return null;
				}	
			}
		}

		if(i < l - 1) return null;
		
		return ZonedDateTime.of(year, month, day, hour, minute, second, nano, zone);
	}
}
