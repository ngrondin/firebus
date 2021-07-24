package io.firebus.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.interfaces.StreamHandler;

public class StreamSender implements StreamHandler {
	
	public interface CompletionListener {
		public void completed();
		public void error(String message);
	}
	
	protected InputStream inputStream;
	protected StreamEndpoint streamEndpoint;
	protected CompletionListener listener;
	protected int chunkSequence;
	protected byte[] chunkBytes;
	protected int chunkLength;
	protected long bytesSent;
	protected boolean completed;
	protected long start;
	protected long lastLoggedProgress;
	protected String error;
	protected boolean waiting;
	
	public StreamSender(InputStream is, StreamEndpoint sep, CompletionListener l) throws IOException {
		inputStream = is;
		streamEndpoint = sep;
		listener = l;
		init();
	}

	public StreamSender(InputStream is, StreamEndpoint sep) throws IOException {
		inputStream = is;
		streamEndpoint = sep;
		init();
	}

	
	protected void init() throws IOException {
		chunkSequence = -1;
		chunkBytes = new byte[262144];
		chunkLength = 0;
		bytesSent = 0;
		completed = false;
		waiting = false;
		error = null;
		start = System.currentTimeMillis();
		lastLoggedProgress = start;
		streamEndpoint.setHandler(this);
		sendNextChunk();		
	}
	
	
	protected void sendNextChunk() throws IOException {
		chunkSequence++;
		chunkLength = inputStream.read(chunkBytes);
		if(chunkLength > -1) {
			sendChunk();
		} else {
			completed = true;
			Payload chunk = new Payload(new byte[0]);
			chunk.metadata.put("ctl", "complete");
			streamEndpoint.send(chunk);
			//System.out.println("Sender sent complete");
		}
	}
	
	protected void sendChunk() {
		Payload chunk = new Payload(chunkLength == chunkBytes.length ? chunkBytes : Arrays.copyOf(chunkBytes, chunkLength));
		chunk.metadata.put("ctl", "chunk");
		chunk.metadata.put("seq", "" + chunkSequence);
		streamEndpoint.send(chunk);
		//System.out.println("Sender sent " + chunkLength + " bytes " + chunkSequence);
	}
	
	public void receiveStreamData(Payload payload, StreamEndpoint streamEndpoint) {
		try {
			String ctl = payload.metadata.get("ctl");
			//System.out.println("Sender received ctl=" + ctl);
			if(ctl.equals("next")) {
				bytesSent += chunkLength;
				sendNextChunk();
			} else if(ctl.equals("complete")) {
				complete();
			} else if(ctl.equals("resend")) {
				sendChunk();
			} else if(ctl.equals("fail")) {
				fail(payload.metadata.get("error"));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void streamClosed(StreamEndpoint streamEndpoint) {
		if(completed == false) {
			fail("Connection closed unexpectedly");
		} else {
			complete();
		}
	}
	
	protected void fail(String e) {
		error = e + " (life: " + (System.currentTimeMillis() - start) + "ms sent: " + bytesSent + "b chunks: " + chunkSequence + ")";
		streamEndpoint.setHandler(null);
		if(listener != null) 
			listener.error(error);	
		close();
	}
	
	protected void complete() {
		streamEndpoint.setHandler(null);
		if(listener != null) 
			listener.completed();
		close();
	}
	
	private void close() {
		try {
			if(listener == null && !waiting) {
				inputStream.close();
				streamEndpoint.close();
			}
			synchronized(this) {
				this.notify();
			}
		} catch(Exception e) { }
	}

	public void sync() throws FunctionErrorException {
		try {
			waiting = true;
			synchronized(this) {
				this.wait();
			}
			waiting = false;
		} catch(Exception e) {
			throw new FunctionErrorException("Error waiting for stream to complete", e);
		}
		if(error != null)
			throw new FunctionErrorException(error);
	}
}
