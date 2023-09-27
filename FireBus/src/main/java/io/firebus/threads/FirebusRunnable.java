package io.firebus.threads;

public class FirebusRunnable {

	public Runnable runnable;
	public String functionName;
	public long functionExecutionId;
	protected long created;
	public long expiry;
	
	public FirebusRunnable(Runnable r, String fn, long feid, long to) {
		runnable = r;
		functionName = fn;
		functionExecutionId = feid;
		created = System.currentTimeMillis();
		expiry = created + to;
	}
}
