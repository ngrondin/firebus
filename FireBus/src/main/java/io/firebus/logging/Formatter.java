package io.firebus.logging;

import io.firebus.data.DataMap;

public interface Formatter {
	public  String format(int lvl, String event, String msg, DataMap data, Throwable t);
}
