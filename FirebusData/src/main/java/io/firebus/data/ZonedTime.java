package io.firebus.data;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ZonedTime {

	protected int hours;
	protected int minutes;
	protected int seconds;
	protected int nano;
	protected ZoneId zoneId;
	
	public ZonedTime() {
		ZonedDateTime zdt = ZonedDateTime.now();
		hours = zdt.getHour();
		minutes = zdt.getMinute();
		seconds = zdt.getSecond();
		nano = zdt.getNano();
		zoneId = zdt.getZone();
	}
	
	public ZonedTime(int h, int m, int s, int n, String zoneStr) {
		hours = h;
		minutes = m;
		seconds = s;
		nano = n;
		zoneId = ZoneId.of(zoneStr);
	}
	
	public ZonedTime(ZonedTime zt) {
		hours = zt.hours;
		minutes = zt.minutes;
		seconds = zt.seconds;
		nano = zt.nano;
		zoneId = ZoneId.of(zt.zoneId.getId());
	}
	
	public ZonedTime(ZonedDateTime zdt) {
		hours = zdt.getHour();
		minutes = zdt.getMinute();
		seconds = zdt.getSecond();
		nano = zdt.getNano();
		zoneId = zdt.getZone();
	}
	
	public static ZonedTime parse(CharSequence text) {
		String str = text.toString();
		if(str.startsWith("T"))
			str = str.substring(1);
		String timeStr = null;
		String zoneStr = "UTC";
		int pos1 = str.indexOf("[");
		int pos2 = str.indexOf("]");
		int pos3 = str.indexOf("+");
		if(pos3 == -1) pos3 = str.indexOf("-");
		if(pos3 == -1) pos3 = str.indexOf("Z");
		if(pos1 == -1 && pos2 == -1 && pos3 == -1) {
			timeStr = str;
		} else if(pos1 > -1 && pos2 > -1 && pos3 == -1) {
			timeStr = str.substring(0, pos1).trim();
			zoneStr = str.substring(pos1 + 1, pos2);
		} else if(pos1 == -1 && pos2 == -1 && pos3 > -1) {
			timeStr = str.substring(0, pos3).trim();
			zoneStr = str.substring(pos3);
		} else {
			throw new RuntimeException("Bad string format");
		}
		int h = 0;
		int m = 0;
		int s = 0;
		int n = 0;
		if(timeStr != null) {
			String[] timeParts = timeStr.split(":");
			if(timeParts.length > 0)
				h = Integer.parseInt(timeParts[0]); 
			if(timeParts.length > 1)
				m = Integer.parseInt(timeParts[1]); 
			if(timeParts.length > 2) {
				if(timeParts[2].indexOf(".") > -1) {
					String[] subParts = timeParts[2].split("\\.");
					s = Integer.parseInt(subParts[0]); 
					n = Integer.parseInt(subParts[1]) * (int)Math.pow(10D, (double)(9 - subParts[1].length())); 
				} else {
					s = Integer.parseInt(timeParts[2]); 
				}
			}
		}
		return new ZonedTime(h, m, s, n, zoneStr);
	}
	
	public ZonedDateTime atDate(ZonedDateTime zdt) {
		return zdt.withZoneSameInstant(zoneId).withHour(hours).withMinute(minutes).withSecond(seconds).withNano(nano);
	}
	
	public int getHours() {
		return hours;
	}
	
	public int getMinutes() {
		return minutes;
	}
	
	public ZoneId getTimezone() {
		return zoneId;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("T");
		sb.append(pad(hours, 2));
		sb.append(":");
		sb.append(pad(minutes, 2));
		sb.append(":");
		sb.append(pad(seconds, 2));
		if(nano > 0) {
			sb.append(".");
			double nd = (double)nano;
			if((nd / 1000000D) - (nano / 1000000) == 0) {
				sb.append(pad(nano / 1000000, 3));
			} else if((nd / 1000D) - (nano / 1000) == 0) {
				sb.append(pad(nano / 1000, 6));
			} else {
				sb.append(pad(nano, 9));
			}
		}
		String zStr = zoneId.getId();
		if(zStr.startsWith("+") || zStr.startsWith("-") || zStr.startsWith("Z")) {
			sb.append(zStr);
		} else {
			sb.append("[");
			sb.append(zStr);
			sb.append("]");
		}
		return sb.toString();
	}
	
	protected String pad(int n, int c) {
		String str = "";
		for(int i = 0; i < c; i++)
			str = str + "0";
		str = str + Integer.toString(n);
		return str.substring(str.length() - c);
	}
}
