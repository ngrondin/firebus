package io.firebus.adapters.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.WebConnection;

public class WebsocketConnectionHandler extends Thread implements HttpUpgradeHandler {
	private Logger logger = Logger.getLogger("io.firebus.adapters.http");
	protected String session;
	protected WebsocketHandler handler;
	protected WebConnection connection;
	protected InputStream is;
	protected OutputStream os;
	protected boolean active;
	
	public void setHandler(WebsocketHandler wsh) {
		handler = wsh;		
	}
	
	public void setSessionId(String sid)
	{
		session = sid;
	}
	
	public void init(WebConnection c) {
		setName("fbHttpWebsocket");
		connection = c;
		try {
			is = connection.getInputStream();
			os = connection.getOutputStream();
			os.flush();
			active = true;
			start();
			logger.fine("Websocket connection created");
		} catch(IOException e) {
			active = false;
		}
	}
	
	public void destroy() {
		handler._onClose(session);
		active = false;
		try {
			is.close();
			os.close();
		} catch(Exception e) {
			
		}
		logger.fine("Websocket connection destroyed");
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
		byte[] msg = new byte[2048];
		int mp = 0;
		int read = -1;
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
							msg[mp] = (byte)(read ^ (maskVal >> (bytePos * 8)));
						} else {
							msg[mp] = (byte)read;
						}
						mp++;
					}
					if(fp == (14 + len - 1)) {
						if(fin == 0) {
							previousOp = op;
						} else {
							if(op == 0)
								op = previousOp;
							if(op == 1) {
								String m = new String(Arrays.copyOfRange(msg, 0, len));
								handler.onStringMessage(session, m);
								mp = 0;
							} else if(op == 2) {
								handler.onBinaryMessage(session, Arrays.copyOfRange(msg, 0, len));
								mp = 0;
							} else if(op == 8) {
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
					} else {
						fp++;
					}
				}
			}
		} catch(Exception e) {
			active = false;
			logger.severe("Websocket connection closed due to exception: " + e.getMessage());
		} finally {
			try {
				connection.close();
			} catch(Exception e) {

			}
		}
	}
	
	private synchronized void send(byte[] msg, int op) {
		int len = msg != null ? msg.length : 0;
		int lenSize = (len <= 125 ? 0 : (len <= 65535 ? 2 : 8));
		try {
			os.write(0x80 | (op & 0x0f));
			if(lenSize == 0) {
				os.write(0x7F & len);
			} else if(lenSize == 2) {
				os.write(0x7F & 126);
				os.write((len >> 8) & 0xff);
				os.write(len & 0xff);
			} else if(lenSize == 8) {
				os.write(0x7F & 127);
				for(int i = 0; i < 8; i++)
					os.write((len >> (7 - i)) & 0xff);
			}
			for(int i = 0; i < len; i++)
				os.write(msg[i]);
			os.flush();
		} catch(Exception e) {
			logger.severe("Websocket exception when sending: " + e.getMessage());
			active = false;
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

}
