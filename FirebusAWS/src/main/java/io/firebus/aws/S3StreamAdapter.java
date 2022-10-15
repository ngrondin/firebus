package io.firebus.aws;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.adapters.Adapter;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.StreamInformation;
import io.firebus.interfaces.StreamProvider;
import io.firebus.utils.StreamReceiver;
import io.firebus.utils.StreamSender;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3StreamAdapter extends Adapter implements StreamProvider {
	private Logger logger = Logger.getLogger("io.firebus.adapters");
	protected Region region;
	protected String bucketName;
	protected String folder;
	protected S3Client s3Client;

	public S3StreamAdapter(DataMap c) {
		super(c);
		bucketName = config.getString("bucket");
		folder = config.getString("folder");
		String regionName = config.getString("region");
		region = Region.of(regionName);
		String accessKey = config.getString("accesskey");
		String secretKey = config.getString("secretkey");
		s3Client = S3Client.builder()
	            .region(region)
	            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
	            .build();
	}


	public Payload acceptStream(Payload payload, final StreamEndpoint streamEndpoint) throws FunctionErrorException {
		try {
			DataMap request = new DataMap(payload.getString());
			String action = request.getString("action");
			final String fileName = request.getString("filename");
			final String filePath = (folder != null ? folder + "/" : "") + fileName;
			
			if(action.equals("get")) {
				GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(filePath).build();
				InputStream is = s3Client.getObject(getObjectRequest);
				new StreamSender(is, streamEndpoint, new StreamSender.CompletionListener() {
					public void completed() {
						cleanup();
					}

					public void error(String message) {
						logger.severe("Error sending file '" + fileName + "': " + message);
						cleanup();
					}
					
					public void cleanup() {
						try {
							streamEndpoint.close();
							is.close();
						} catch(Exception e) {
							logger.severe("Error closing stream after file get : " + e.getMessage());
						}
					}
				});
				return null;
			} else if(action.equals("put")) {
				final File file = new File(fileName);
				final String mime = payload.metadata.get("mime");
				final FileOutputStream fos = new FileOutputStream(file);
				new StreamReceiver(fos, streamEndpoint, new StreamReceiver.CompletionListener() {
					public void completed() {
						Map<String, String> metadata = new HashMap<String, String>();
						if(mime != null) 
					        metadata.put("content-type", mime);
						PutObjectRequest objectRequest = PutObjectRequest.builder().bucket(bucketName).key(filePath).metadata(metadata).build();
				        s3Client.putObject(objectRequest, RequestBody.fromFile(file));
				        cleanup();
					}

					public void error(String message) {
						logger.severe("Error putting file '" + fileName + "': " + message);
						cleanup();
					}
					
					public void cleanup() {
						try {
							streamEndpoint.close();
							fos.close();
					        file.delete();
						} catch(Exception e) {
							logger.severe("Error closing stream after file put : " + e.getMessage());
						}	
					}
				});
				
				return null;
			} else {
				throw new FunctionErrorException("No action provided");
			}
		} catch(Exception e) {
			logger.severe("Error accepting stream connection : " + e.getMessage());
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
