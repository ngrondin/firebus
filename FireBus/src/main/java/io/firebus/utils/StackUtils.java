package io.firebus.utils;

public class StackUtils {
	
	public static String getStackString() {
		StackTraceElement[] elements = Thread.getAllStackTraces().get(Thread.currentThread());
		boolean started = false;
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < elements.length; i++) {
			if(started) {
				if(sb.length() > 0) sb.append("\r\n");
				sb.append(elements[i].getClassName());
				sb.append(".");
				sb.append(elements[i].getMethodName());
				sb.append("(");
				sb.append(elements[i].getFileName());
				sb.append(":");
				sb.append(elements[i].getLineNumber());
				sb.append(")");
			} else {
				if(elements[i].getMethodName().equals("getStackString")) 
					started = true;
			}
		}
		return sb.toString();
	}

}
