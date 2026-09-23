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
		chunkSequence++;
		if(bufferSize > -1) {
			sendChunk();
		}
	}
	
	protected void sendChunk() {
		Payload chunk = new Payload(bufferSize == buffer.length ? buffer : Arrays.copyOf(buffer, bufferSize));
		chunk.metadata.put("ctl", "chunk");
		chunk.metadata.put("seq", "" + chunkSequence);
		streamEndpoint.send(chunk);
	}
	
	public void receiveStreamData(Payload payload) {
		try {
			String ctl = payload.metadata.get("ctl");
			if(ctl.equals("next")) {
				bufferSize = 0;
				notif();
			} else if(ctl.equals("complete")) {
				completionBytes = payload.getBytes();
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
		notif();
	}
	
	public void write(int val) throws IOException {
		buffer[bufferSize++] = (byte)(0x00ff & val);
		if(bufferSize == buffer.length)
			flush();
	}
	
	public void flush() throws IOException{
		sendNextChunk();
		waitForSending();
	}
	
	public void close() throws IOException {
		flush();
		completed = true;
		Payload chunk = new Payload(new byte[0]);
		chunk.metadata.put("ctl", "complete");
		streamEndpoint.send(chunk);
	}

	public byte[] getCompletionBytes() {
		return completionBytes;
	}
	
	private void waitForSending() {
		try {
			synchronized(this) {
				waiting = true;
				while(waiting) 
					wait(1000);
			}
		} catch(Exception e) {}
	}
	
	private void notif() {
		try {
			synchronized(this) {
				waiting = false;
				notifyAll();
			}
		} catch(Exception e) {}		
	}
}
