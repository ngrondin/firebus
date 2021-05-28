package io.firebus.threads;

public class FirebusRunnable {

	public Runnable runnable;
	public long expiry;
	
	public FirebusRunnable(Runnable r, long e) {
		runnable = r;
		expiry = e;
	}
}
