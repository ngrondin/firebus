package io.firebus.utils;

import java.io.IOException;
import java.util.Arrays;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.interfaces.StreamHandler;

public class OutputStream extends java.io.OutputStream implements StreamHandler{
	protected StreamEndpoint streamEndpoint;
	protected byte[] buffer;
	protected int readHead;
	protected int bufferSize;
	protected int chunkSequence;
	protected boolean completed;
	protected long start;
	protected String error;
	protected boolean waiting;
	protected byte[] completionBytes;
	
	public OutputStream(StreamEndpoint sep) {
		streamEndpoint = sep;
		buffer = new byte[32768];
		bufferSize = 0;
		chunkSequence = -1;
		completed = false;
		waiting = false;
		error = null;
		streamEndpoint.setHandler(this);
	}
	
	protected void sendNextChunk() throws IOException {
		if(bufferSize > 0) {
			chunkSequence++;
			sendChunk();
		}
	}
	
	protected void sendChunk() {
		byte[] bytes = bufferSize == buffer.length ? buffer : Arrays.copyOf(buffer, bufferSize);
		send("chunk", chunkSequence, bytes);
	}
	
	protected void send(String ctl, int seq, byte[] bytes) {
		Payload payload = new Payload(bytes);
		payload.metadata.put("ctl", ctl);
		if(seq >= 0)
			payload.metadata.put("seq", String.valueOf(seq));
		waiting = true;
		streamEndpoint.send(payload);
	}
	
	public void receiveStreamData(Payload payload) {
		try {
			String ctl = payload.metadata.get("ctl");
			if(ctl.equals("next")) {
				bufferSize = 0;
				waiting = false;
				notif("from next");
			} else if(ctl.equals("complete")) {
				completionBytes = payload.getBytes();
				completed = true;
				waiting = false;
				notif("from complete");
			} else if(ctl.equals("resend")) {
				sendChunk();
			} else if(ctl.equals("fail")) {
				fail(payload.metadata.get("error"));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void streamClosed() {
		if(completed == false)
			fail("Stream Sender Connection closed before completion");
	}
	
	public void streamError(FunctionErrorException error) {
		fail(error.getMessage());	
	}
	private void fail(String msg) {
		error = msg;
		notif("from fail");
	}
	
	public void write(int val) throws IOException {
		waitForSending("initial waiting while writing");
		if(bufferSize == buffer.length) {
			flush();
			waitForSending("after write flush");
		}
		buffer[bufferSize++] = (byte)(0x00ff & val);
		if(bufferSize == buffer.length) {
			flush();
		}
	}
	
	public void flush() throws IOException{
		waitForSending("initial waiting while flushing");
		sendNextChunk();
	}
	
	public void close() throws IOException {
		flush();
		waitForSending("after close flushing");
		completed = true;
		send("complete", -1, null);
		waitForSending("after complete sent");
		streamEndpoint.close();
	}

	public byte[] getCompletionBytes() {
		return completionBytes;
	}
	
	private void waitForSending(String msg) {
		if(waiting) {
			try {
				synchronized(this) {
					while(waiting) 
						wait(1000);
				}
			} catch(Exception e) {}			
		}
	}
	
	private void notif(String msg) {
		try {
			synchronized(this) {
				notifyAll();
			}
		} catch(Exception e) {}		
	}
}
