package io.firebus.utils;

import java.io.OutputStream;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.interfaces.StreamHandler;

public class StreamReceiver implements StreamHandler {

	public interface CompletionListener {
		public void completed();
		public void error(String message);
	}
	
	public interface ChunkListener {
		public void chunk(byte[] bytes);
		public void completed();
		public void error(String message);
	}
	
	protected OutputStream outputStream;
	protected StreamEndpoint streamEndpoint;
	protected CompletionListener compListener;
	protected ChunkListener chunkListener;
	protected long bytesReceived;
	protected long start;
	protected int chunkSequence;
	protected boolean complete;
	protected long lastLoggedProgress;
	protected boolean waiting;
	protected String error;
	
	public StreamReceiver(OutputStream os, StreamEndpoint sep) {
		outputStream = os;
		streamEndpoint = sep;
		init();
	}
	
	public StreamReceiver(OutputStream os, StreamEndpoint sep, CompletionListener l) {
		outputStream = os;
		streamEndpoint = sep;
		compListener = l;
		init();
	}
	
	public StreamReceiver(StreamEndpoint sep, ChunkListener l) {
		streamEndpoint = sep;
		chunkListener = l;
		init();
	}
	
	private void init() {
		bytesReceived = 0;
		chunkSequence = 0;
		complete = false;
		waiting = false;
		error = null;
		start = System.currentTimeMillis();
		lastLoggedProgress = start;
		streamEndpoint.setHandler(this);		
	}
	
	public void receiveStreamData(Payload payload, StreamEndpoint streamEndpoint) {
		try {
			byte[] bytes = payload.getBytes();
			String ctl = payload.metadata.get("ctl");
			if(ctl.equals("chunk")) {
				if(payload.metadata.containsKey("seq")) {
					int seq = Integer.parseInt(payload.metadata.get("seq"));
					if(seq == chunkSequence) {
						bytesReceived += bytes.length;
						if(outputStream != null) {
							outputStream.write(bytes);	
							outputStream.flush();
						} else if(chunkListener != null) {
							chunkListener.chunk(bytes);
						}
						chunkSequence++;
						Payload resp = new Payload();
						resp.metadata.put("ctl", "next");
						streamEndpoint.send(resp);
						long now = System.currentTimeMillis();
						if(lastLoggedProgress < now - 10000) {
							lastLoggedProgress = now;
						}
					} else {
						fail("Chunk out of sequence");
					}
				} else {
					fail("Missing sequence number");
				}
			} else if(ctl.equals("complete")) {
				complete = true;
				streamEndpoint.setHandler(null);
				if(compListener != null)
					compListener.completed();
				else if(chunkListener != null)
					chunkListener.completed();
				done();
			}
		} catch(Exception e) {
			fail(e.getMessage());
		}		
	}

	public void streamClosed(StreamEndpoint streamEndpoint) {
		if(complete == false)
			fail("Connection unexpectedly closed");		
	}
	
	protected void fail(String e) {
		error = e;
		streamEndpoint.setHandler(null);
		if(compListener != null)
			compListener.error(error);
		else if(chunkListener != null)
			chunkListener.error(error);
		done();
	}
	
	private void done() {
		try {
			if(compListener == null && chunkListener == null && !waiting) {
				outputStream.close();
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
