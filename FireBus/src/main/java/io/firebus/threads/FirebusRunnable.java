package io.firebus.threads;

public class FirebusRunnable {

	public Runnable runnable;
	public String functionName;
	public long functionExecutionId;
	public long expiry;
	
	public FirebusRunnable(Runnable r, String fn, long feid, long e) {
		runnable = r;
		functionName = fn;
		functionExecutionId = feid;
		expiry = e;
	}
}
