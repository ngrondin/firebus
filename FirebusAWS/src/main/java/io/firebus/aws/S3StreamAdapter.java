package io.firebus.aws;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.adapters.Adapter;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.StreamInformation;
import io.firebus.interfaces.StreamProvider;
import io.firebus.logging.Logger;
import io.firebus.utils.StreamSender;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

public class S3StreamAdapter extends Adapter implements StreamProvider {
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
			DataMap request = payload.getDataMapOrNull();
			if(request == null) request = new DataMap();
			String action = request.getString("action");
			final String fileName = request.getString("filename");
			final String filePath = (folder != null ? folder + "/" : "") + fileName;
			
			if(action.equals("get")) {
				GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(filePath).build();
				InputStream is = s3Client.getObject(getObjectRequest);
				new StreamSender(is, streamEndpoint, new StreamSender.CompletionListener() {
					public void completed(byte[] bytes) {

					}

					public void error(Throwable error) {
						Logger.severe("fb.adapter.aws.s3.errorsending", new DataMap("file", fileName), error);
					}
				});
				return null;
			} else if(action.equals("put")) {
				final io.firebus.utils.InputStream is = new io.firebus.utils.InputStream(streamEndpoint);
				final int size = is.available();
				Logger.info("fb.adapter.aws.s3.put", new DataMap("file", fileName, "size", size, "available", is.available(), "read", is.getTotalRead()));
				Map<String, String> metadata = new HashMap<String, String>();
				if(payload.metadata.containsKey("mime")) 
			        metadata.put("content-type", payload.metadata.get("mime"));
				PutObjectRequest objectRequest = PutObjectRequest.builder().bucket(bucketName).key(filePath).metadata(metadata).build();
				new Thread(new Runnable() {
					public void run() {
						try {
							s3Client.putObject(objectRequest, RequestBody.fromInputStream(is, size));
						} catch(Exception e) {
							Logger.severe("fb.adapter.aws.s3.put", new DataMap("file", fileName, "size", size, "available", is.available(), "read", is.getTotalRead()), e);
						}				        
					}
				}).start();				
				return null;
			} else {
				throw new FunctionErrorException("No action provided");
			}
		} catch(S3Exception e) {
			throw new FunctionErrorException("Error calling S3 service", e, e.statusCode());
		} catch(IOException e) {
			throw new FunctionErrorException("Error streaming file", e);
		} 
	}

	public int getStreamIdleTimeout() {
		return 15000;
	}

	public StreamInformation getStreamInformation() {
		return null;
	}

}
