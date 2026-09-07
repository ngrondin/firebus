package io.firebus.utils;

import java.io.IOException;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.data.DataException;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.interfaces.StreamHandler;

public class InputStream extends java.io.InputStream implements StreamHandler {
	protected StreamEndpoint streamEndpoint;
	protected byte[] buffer;
	protected int readHead;
	protected int bufferSize;
	protected int chunkSequence;
	protected boolean waiting;
	protected boolean complete;
	protected String error;
	protected int totalRead;
	protected int totalSize;
	protected String filename;
	protected String mime;

	
	public InputStream(StreamEndpoint sep) {
		streamEndpoint = sep;
		buffer = new byte[32768];
		readHead = 0;
		bufferSize = 0;
		chunkSequence = 0;
		complete = false;
		waiting = true; //The initial chunk is sent by the sender first, so we're initially waiting
		totalRead = 0;
		totalSize = -1;
		if(sep.getAcceptPayload() != null || sep.getRequestPayload() != null) {
			try {
				Payload payload = sep.getAcceptPayload();
				if(payload == null) payload = sep.getRequestPayload();
				if(payload != null) {
					DataMap acceptMap = payload.getDataMap();
					filename = acceptMap.getString("filename");
					mime = acceptMap.getString("mime");
					if(acceptMap.containsKey("size"))
						totalSize = acceptMap.getNumber("size").intValue();	
				}
			} catch (DataException e) { }			
		}
		streamEndpoint.setHandler(this);
	}

	public int read() throws IOException {
		if(complete) return -1;
		if(readHead == bufferSize) {
			readHead = 0;
			bufferSize = 0;
			sendNext();
			try {
				synchronized(this) {
					while(waiting) {
						wait(10000);
					}
				}
			} catch(Exception e) {}
			if(error != null) throw new IOException(error);
			if(complete) return -1;
			if(readHead == bufferSize) throw new IOException("Did not receive the next chunk");
		} 	
		int val = (buffer[readHead] & 0xFF);
		totalRead++;
		readHead++;
		if(readHead == bufferSize) {
			sendNext();
		}
		return val;
	}
	
	private void sendNext() {
		if(!waiting) {
			Payload resp = new Payload();
			resp.metadata.put("ctl", "next");
			streamEndpoint.send(resp);
			waiting = true;
		}
	}

	public void receiveStreamData(Payload payload) {
		waiting = false;
		byte[] bytes = payload.getBytes();
		String ctl = payload.metadata.get("ctl");
		if(ctl.equals("chunk")) {
			if(payload.metadata.containsKey("seq")) {
				int seq = Integer.parseInt(payload.metadata.get("seq"));
				if(seq == chunkSequence) {
					for(int i = 0; i < bytes.length; i++)
						buffer[bufferSize++] = bytes[i];						
					chunkSequence++;
				} else {
					error = "Chunk out of sequence";
				}
			} else {
				error = "Missing sequence number";
			}
		} else if(ctl.equals("complete")) {
			complete = true;
			Payload resp = new Payload();
			resp.metadata.put("ctl", "complete");
			streamEndpoint.send(resp);
		}
		notif();
	}

	public void streamClosed() {
		waiting = false;
		if(complete == false) {
			error = "Stream Receiver connection closed before completion";
			complete = true;
		}
		notif();
	}

	public void streamError(FunctionErrorException funcError) {
		waiting = false;
		error = funcError.getMessage();
		notify();
	}
	
	private void notif() {
		try {
			synchronized(this) {
				notify();
			}
		} catch(Exception e) {}		
	}
	
	public void close() throws IOException {
		super.close();
		streamEndpoint.close();
	}

	public int available() {
		if(totalSize > -1)
			return totalSize - totalRead;
		else 
			return bufferSize - readHead;
	}
	
	public int getSize() {
		return totalSize;
	}
	
	public int getTotalRead() {
		return totalRead;
	}
	
	public String getFileName() {
		return filename;
	}
	
	public String getMime() {
		return mime;
	}
}
