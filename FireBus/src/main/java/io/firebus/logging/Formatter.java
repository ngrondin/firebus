package io.firebus.logging;

public interface Formatter {
	public  String format(int lvl, String event, Object data, Throwable t);
}
