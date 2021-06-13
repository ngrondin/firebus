package io.firebus.information;

import io.firebus.Address;

public class KnownAddressInformation {
	protected Address address;
	protected long lastTry;
	protected long nextTry;
	protected int failedCount;
	protected boolean self;
	
	public KnownAddressInformation(Address a) {
		address = a;
		lastTry = 0;
		nextTry = 0;
		failedCount = 0;
		self = false;
	}
	
	public Address getAddress() {
		return address;
	}
	
	public boolean isDueToTry() {
		long now = System.currentTimeMillis();
		return now > nextTry;
	}
	
	public void connectionFailed() {
		failedCount++;
		long now = System.currentTimeMillis();
		if(failedCount <= 3) {
			nextTry = now + 2000;
		} else if(failedCount <= 15) {
			nextTry = now + (2000 * (failedCount - 3));
		} else {
			nextTry = now + 30000;
		}
	}
	
	public void connectionSucceeded() {
		failedCount = 0;
	}
	
	public boolean isSelf() {
		return self;
	}
	
	public void setAsSelf() {
		self = true;
	}

}
