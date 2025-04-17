package io.firebus.adapters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.StreamInformation;
import io.firebus.interfaces.StreamProvider;
import io.firebus.logging.Logger;
import io.firebus.data.DataMap;
import io.firebus.utils.StreamReceiver;
import io.firebus.utils.StreamSender;

public class FileStreamAdapter extends Adapter implements StreamProvider {
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
					public void completed(byte[] bytes) {

					}

					public void error(String message) {
						Logger.severe("fb.adapter.filestream.senderror", new DataMap("file", fileName, "msg", message));
					}
				});
				return null;
			} else if(action.equals("put")) {
				final FileOutputStream fos = new FileOutputStream(path + File.separator + fileName);
				new StreamReceiver(fos, streamEndpoint, new StreamReceiver.CompletionListener() {
					public byte[] completed() {

						return null;
					}

					public void error(String message) {
						Logger.severe("fb.adapter.filestream.puterror", new DataMap("file", fileName, "msg", message));
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
