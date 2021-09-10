package io.firebus;

import io.firebus.threads.FirebusThread;

public class RouteMessage implements Runnable {
	protected NodeCore nodeCore;
	protected Message message;
	protected long created;
	
	public RouteMessage(NodeCore nc, Message msg) {
		nodeCore = nc;
		message = msg;
		created = System.currentTimeMillis();
	}
	
	public void run() {
		String trackingId = message.getTypeString() + ":" + message.getSubject();
		if(Thread.currentThread() instanceof FirebusThread) 
			((FirebusThread)Thread.currentThread()).setTrackingId(trackingId);
		long start = System.currentTimeMillis();
		nodeCore.route(message);
		long end = System.currentTimeMillis();
		long dur = end - start;
		long totalDur = end - created;
		if(dur > 3 || totalDur > 4) {
			System.err.println("Message routing (" + trackingId + ") took a long time: " + totalDur + "ms (" + dur + "ms)");
		}
	}

}
