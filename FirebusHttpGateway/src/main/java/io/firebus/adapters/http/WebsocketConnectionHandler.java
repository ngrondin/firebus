package io.firebus.adapters.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.WebConnection;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.logging.Logger;

public abstract class WebsocketConnectionHandler extends Thread implements HttpUpgradeHandler {
	protected String id;
	protected WebsocketHandler handler;
	protected Map<String, String> parameters;
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
	
	public void configure(WebsocketHandler wsh, HttpServletRequest req) throws ServletException, IOException {
		handler = wsh;
		parameters = HttpHandler.getParams(req);
		requestPayload = new Payload();
    	if(handler.getSecurityHandler() != null)
    		handler.getSecurityHandler().enrichFirebusRequest(req, requestPayload);
    	HttpHandler.enrichFirebusRequestDefault(req, requestPayload);
	}

	
	public Firebus getFirebus() {
		return handler.firebus;
	}
	
	public DataMap getConfig() {
		return handler.handlerConfig;
	}
	
	protected Payload getRequestPayload() throws ServletException, IOException {
    	return requestPayload;
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
			Logger.fine("fb.http.ws.connhandler.created", new DataMap("id", id));
		} catch(Exception e) {
			Logger.severe("fb.http.ws.connhandler.creating", new DataMap("id", id), e);
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
				Logger.fine("fb.http.ws.connhandler.destroyed", new DataMap("id", id));
			} catch(ClosedChannelException e) {
				
			} catch(Exception e) {
				Logger.warning("fb.http.ws.connhandler.destroying", new DataMap("id", id), e);
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
				Logger.warning("fb.http.ws.run", new DataMap("id", id), e);
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
				Logger.warning("fb.http.ws.send", new DataMap("id", id), e);
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

	
	protected abstract void onOpen() throws FunctionErrorException, FunctionTimeoutException, ServletException, IOException;
	protected abstract void onStringMessage(String msg) throws FunctionErrorException, FunctionTimeoutException;
	protected abstract void onBinaryMessage(byte[] msg) throws FunctionErrorException, FunctionTimeoutException;
	protected abstract void onClose() throws FunctionErrorException, FunctionTimeoutException;

}
