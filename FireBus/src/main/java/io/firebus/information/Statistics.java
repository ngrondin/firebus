package io.firebus.information;

public class Statistics {
	public String name;
	public int lastPeriodCount;
	public int maxCount;
	public int limitCount;
	
	public Statistics(String n, int lpc, int mc, int lc) {
		name = n;
		lastPeriodCount = lpc;
		maxCount = mc;
		limitCount = lc;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append(":");
		sb.append(lastPeriodCount);
		sb.append("/");
		sb.append(maxCount);
		sb.append("/");
		sb.append(limitCount);
		sb.append(";");
		return sb.toString();
	}
}
