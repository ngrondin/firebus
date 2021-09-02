package io.firebus;

public class RouteMessage implements Runnable {
	protected NodeCore nodeCore;
	protected Message message;
	
	public RouteMessage(NodeCore nc, Message msg) {
		nodeCore = nc;
		message = msg;
	}
	
	public void run() {
		long start = System.currentTimeMillis();
		nodeCore.route(message);
		long end = System.currentTimeMillis();
		long dur = end - start;
		if(dur > 1) {
			System.err.println("Message routing (" + message.getTypeString() + ":" + message.getSubject() + ") took a long time: " + dur + "ms");
		}
	}

}
