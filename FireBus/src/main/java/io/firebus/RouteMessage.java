package io.firebus;

import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
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
		if(dur > 5 || totalDur > 10) {
			Logger.warning("fb.thread.route.long", new DataMap("routing", totalDur, "total", totalDur));
		}
	}

}
