package io.firebus.utils;

import java.io.OutputStream;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.interfaces.StreamHandler;

public class StreamReceiver implements StreamHandler {

	public interface CompletionListener {
		public byte[] completed() throws Exception;
		public void error(String message);
	}
	
	public interface ChunkListener {
		public void chunk(byte[] bytes);
		public byte[] completed() throws Exception;
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
	
	public void receiveStreamData(Payload payload) {
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
				byte[] completionbytes = complete();
				Payload resp = new Payload(completionbytes);
				resp.metadata.put("ctl", "complete");
				streamEndpoint.send(resp);
				complete = true;
			}
		} catch(Exception e) {
			fail(e.getMessage());
		}		
	}

	public void streamClosed() {
		if(complete == false)
			fail("Stream Receiver connection closed before completion");	
		else
			close();
	}
	
	public void streamError(FunctionErrorException error) {
		fail(error.getMessage());
	}

	
	protected void fail(String e) {
		error = e + " (life: " + (System.currentTimeMillis() - start) + "ms received: " + bytesReceived + "b chunks: " + chunkSequence + ")";
		streamEndpoint.error(new FunctionErrorException(e));
		if(compListener != null)
			compListener.error(error);
		else if(chunkListener != null)
			chunkListener.error(error);
		close();
	}
	
	protected byte[] complete() throws Exception {
		if(compListener != null)
			return compListener.completed();
		else if(chunkListener != null)
			return chunkListener.completed();
		return null;
	}
	
	private void close() {
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
	
	public long getBytesReceived() {
		return bytesReceived;
	}

}
