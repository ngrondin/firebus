package io.firebus;

public class RouteMessage implements Runnable {
	protected NodeCore nodeCore;
	protected Message message;
	
	public RouteMessage(NodeCore nc, Message msg) {
		nodeCore = nc;
		message = msg;
	}
	
	public void run() {
		nodeCore.route(message);
	}

}
