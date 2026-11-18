package io.firebus.adapters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.information.StreamInformation;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.interfaces.StreamProvider;
import io.firebus.logging.Logger;
import io.firebus.data.DataMap;
import io.firebus.utils.StreamReceiver;
import io.firebus.utils.StreamSender;

/**
 * Files in a local folder.
 * - stream "get" and "put": read or write one file
 * - service "delete": remove one file, {"action":"delete", "filename":...}
 */
public class FileStreamAdapter extends Adapter implements StreamProvider, ServiceProvider {
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

					public void error(Throwable error) {
						Logger.severe("fb.adapter.filestream.senderror", new DataMap("file", fileName, "msg"), error);
					}
				});
				return null;
			} else if(action.equals("put")) {
				final FileOutputStream fos = new FileOutputStream(path + File.separator + fileName);
				new StreamReceiver(fos, streamEndpoint, new StreamReceiver.CompletionListener() {
					public byte[] completed() {
						return null;
					}

					public void error(Throwable error) {
						Logger.severe("fb.adapter.filestream.puterror", new DataMap("file", fileName), error);
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

	public Payload service(Payload payload) throws FunctionErrorException {
		try {
			DataMap request = new DataMap(payload.getString());
			String fileName = request.getString("filename");
			if(!"delete".equals(request.getString("action")) || fileName == null)
				throw new FunctionErrorException("Only the delete action with a filename is supported");
			new File(path + File.separator + fileName).delete();
			return new Payload(new DataMap("result", "ok"));
		} catch(FunctionErrorException e) {
			throw e;
		} catch(Exception e) {
			throw new FunctionErrorException("Error deleting file", e);
		}
	}

	public ServiceInformation getServiceInformation() {
		return null;
	}

	public int getStreamIdleTimeout() {
		return 5000;
	}

	public StreamInformation getStreamInformation() {
		return null;
	}

}
