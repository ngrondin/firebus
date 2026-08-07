package io.firebus.threads;

import io.firebus.data.DataMap;

public class FirebusRunnable {

	public Runnable runnable;
	public String functionName;
	public long functionExecutionId;
	public long created;
	public long pushed;
	public long poped;
	public long started;
	public long longExecWarningTime;
	public long expiry;
	public DataMap logData;
	
	public FirebusRunnable(Runnable r, String fn, long feid, long to, long wt, DataMap ld) {
		runnable = r;
		functionName = fn;
		functionExecutionId = feid;
		created = System.currentTimeMillis();
		longExecWarningTime = wt;
		expiry = created + to;
		logData = ld;
	}
}
