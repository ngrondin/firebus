package io.firebus.adapters.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.WebConnection;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.data.DataMap;

public abstract class WebsocketConnectionHandler extends Thread implements HttpUpgradeHandler {
	private Logger logger = Logger.getLogger("io.firebus.adapters.http");
	protected String id;
	protected WebsocketHandler handler;
	protected Payload requestPayload;
	protected WebConnection connection;
	protected InputStream is;
	protected OutputStream os;
	protected boolean active;
	protected boolean destroyed;
	
	public WebsocketConnectionHandler() {
		id = UUID.randomUUID().toString();
		active = false;
		destroyed = false;
	}
	
	public void configure(WebsocketHandler wsh, Payload p) {
		handler = wsh;
		requestPayload = p;
	}

	public Payload getRequestPayload() {
		return requestPayload;
	}
	
	public Firebus getFirebus() {
		return handler.firebus;
	}
	
	public DataMap getConfig() {
		return handler.handlerConfig;
	}

	public void init(WebConnection c) {
		setName("fbHttpWebsocket");
		connection = c;
		try {
			is = connection.getInputStream();
			os = connection.getOutputStream();
			os.flush();
			onOpen();
			start();
			logger.fine("Websocket connection created");
		} catch(Exception e) {
			logger.severe("Error initating websocket connection " + id + ": " + e.getMessage());
			active = false;
			destroy();
		} 
	}
	
	public void destroy() {
		if(!destroyed) {
			destroyed = true;
			active = false;
			try {
				onClose();
				is.close();
				os.close();
				connection.close();
				logger.fine("Websocket connection destroyed");
			} catch(Exception e) {
				logger.fine("Error destroying websocket connection " + id + ": " + e.getMessage());
			}
		}
	}
	
	public String getConnectionId() {
		return id;
	}

	public void run() {
		int fp = 0;
		int fin = 0;
		int op = 0;
		int previousOp = 0;
		int len = 0;
		int lenSize = 0;
		int mask = 0;
		int maskVal = 0;
		List<ByteBuffer> frames = new ArrayList<ByteBuffer>();
		int runningLen = 0;
		ByteBuffer frame = null;
		int read = -1;
		active = true;
		try {
			while(active) {
				read = is.read();
				if(read == -1) {
					active = false;
				} else {
					if(fp == 0) {
						fin = (read >> 7) & 0x01;
						op = read & 0x0f;
					} else if(fp == 1) {
						mask = (read >> 7) * 0x01;
						if((read & 0x7F) <= 125) {
							len = read & 0x7F;
							lenSize = 0;
							fp = 9;
						} else if((read & 0x7F) == 126) {
							lenSize = 2;
						} else if((read & 0x7F) == 127) {
							lenSize = 8;
						}
					} else if(fp >= 2 && fp < 10) {
						len = (len << 8) | read;
						if(lenSize == 2 && fp == 3)
							fp = 9;
					} else if(fp >= 10 && fp < 14) {
						if(mask == 1)
							maskVal = (maskVal << 8) | read;
						else
							fp = 13;
					} else if(fp >= 14 && fp < (14 + len)) {
						if(mask == 1) {
							int bytePos = 3 - ((fp - 14) % 4);
							frame.put((byte)(read ^ (maskVal >> (bytePos * 8))));
						} else {
							frame.put((byte)read);
						}
					}
					
					if(fp < (14 + len)) {
						if(fp == 9) {
							//System.out.println("WS op = " + op + "  fin = " + fin + "  len = " + len);
							frame = ByteBuffer.allocate(len);
							runningLen += len;
						}
						fp++;	
					}
					if(fp == (14 + len)){
						if(fin == 0) {
							frames.add(frame);
							if(op != 0) 
								previousOp = op;
						} else {
							if(op == 0) {
								frames.add(frame);
								frame = ByteBuffer.allocate(runningLen);
								for(ByteBuffer bb : frames)
									frame.put(bb.array());
								op = previousOp;
								runningLen = 0;
								frames.clear();
							}
							if(op == 1) {
								String m = new String(frame.array());
								onStringMessage(m);
							} else if(op == 2) {
								onBinaryMessage(frame.array());
							} else if(op == 8) {
								send(frame.array(), 8);
								active = false;
							} else if(op == 9) {
								send(null, 10);
							}
						}
						fp = 0;
						fin = 0;
						op = 0;
						len = 0;
						lenSize = 0;
						mask = 0;
						maskVal = 0;						
					}
				}
			}
		} catch(Exception e) {
			if(active) {
				active = false;
				logger.warning("Websocket connection " + id + " closed due to exception: " + e.getMessage());
			}
		} finally {
			destroy();
		}
	}
	
	private synchronized void send(byte[] msg, int op) {
		if(active && os != null) {
			long len = msg != null ? msg.length : 0;
			int lenSize = (len <= 125 ? 0 : (len <= 65535 ? 2 : 8));
			try {
				os.write(0x80 | (op & 0x0f));
				if(lenSize == 0) {
					os.write((int)(0x7F & len));
				} else if(lenSize == 2) {
					os.write((int)(0x7F & 126));
					os.write((int)((len >> 8) & 0xff));
					os.write((int)(len & 0xff));
				} else if(lenSize == 8) {
					os.write(0x7F & 127);
					for(int s = 56; s >= 0; s-=8) {
						os.write((int)((len >> s) & 0xff));
					}
				}
				os.write(msg, 0, (int)len);
				os.flush();
			} catch(Exception e) {
				logger.warning("Websocket exception when sending: " + e.getMessage());
				active = false;
			}			
		} 
	}
	
	public void sendStringMessage(String msg)
	{
		send(msg.getBytes(), 1);
	}
	
	public void sendBinaryMessage(byte[] bytes)
	{
		send(bytes, 2);
	}

	
	protected abstract void onOpen() throws FunctionErrorException, FunctionTimeoutException;
	protected abstract void onStringMessage(String msg) throws FunctionErrorException, FunctionTimeoutException;
	protected abstract void onBinaryMessage(byte[] msg) throws FunctionErrorException, FunctionTimeoutException;
	protected abstract void onClose() throws FunctionErrorException, FunctionTimeoutException;

}
