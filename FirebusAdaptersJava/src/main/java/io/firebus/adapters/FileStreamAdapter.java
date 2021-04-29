package io.firebus.adapters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.logging.Logger;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.StreamInformation;
import io.firebus.interfaces.StreamProvider;
import io.firebus.utils.DataMap;
import io.firebus.utils.StreamReceiver;
import io.firebus.utils.StreamSender;

public class FileStreamAdapter extends Adapter implements StreamProvider {
	private Logger logger = Logger.getLogger("io.firebus.adapters");
	protected String path;
	
	public FileStreamAdapter(DataMap c) {
		super(c);
		path = config.getString("path");
	}

	public Payload acceptStream(Payload payload, final StreamEndpoint streamEndpoint) throws FunctionErrorException {
		try {
			DataMap request = new DataMap(payload.getString());
			String action = request.getString("action");
			String fileName = request.getString("filename");
			if(action.equals("get")) {
				final FileInputStream fis = new FileInputStream(path + File.separator + fileName);
				new StreamSender(fis, streamEndpoint, new StreamSender.CompletionListener() {
					public void completed() {
						try {
							streamEndpoint.close();
							fis.close();
						} catch(Exception e) {
							logger.severe("Error closing stream after file get : " + e.getMessage());
						}
					}

					public void error(String message) {
						logger.severe("Error getting file : " + message);
					}
				});
				return null;
			} else if(action.equals("put")) {
				final FileOutputStream fos = new FileOutputStream(path + File.separator + fileName);
				new StreamReceiver(fos, streamEndpoint, new StreamReceiver.CompletionListener() {
					public void completed() {
						try {
							streamEndpoint.close();
							fos.close();
						} catch(Exception e) {
							logger.severe("Error closing stream after file put : " + e.getMessage());
						}						
					}

					public void error(String message) {
						logger.severe("Error putting file : " + message);
					}
				});
				return null;
			} else {
				throw new FunctionErrorException("No action provided");
			}
		} catch(Exception e) {
			throw new FunctionErrorException("Error accepting stream connection", e);
		}
	}

	public int getStreamIdleTimeout() {
		return 5000;
	}

	public StreamInformation getStreamInformation() {
		return null;
	}

}
