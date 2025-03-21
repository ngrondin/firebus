package io.firebus.information;

import io.firebus.Address;

public class KnownAddressInformation {
	protected Address address;
	protected long lastTry;
	protected long nextTry;
	protected boolean onceConnected;
	protected int failedCount;
	protected boolean self;
	
	public KnownAddressInformation(Address a) {
		address = a;
		lastTry = 0;
		nextTry = 0;
		failedCount = 0;
		onceConnected = false;
		self = false;
	}
	
	public Address getAddress() {
		return address;
	}
	
	public int tries() {
		return failedCount;
	}
	
	public boolean isDueToTry() {
		return !self && System.currentTimeMillis() > nextTry;
	}

	public boolean isSelf() {
		return self;
	}
	
	public boolean shouldRemove() {
		return onceConnected && failedCount > 30 || !onceConnected && failedCount > 3;
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
		onceConnected = true;
	}
	
	public void setAsSelf() {
		self = true;
	}
	
	public String toString() {
		return address.toString();
	}

}
