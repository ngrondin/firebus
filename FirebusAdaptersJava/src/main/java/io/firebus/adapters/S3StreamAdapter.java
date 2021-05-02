package io.firebus.adapters;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.logging.Logger;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.StreamInformation;
import io.firebus.interfaces.StreamProvider;
import io.firebus.utils.DataMap;
import io.firebus.utils.StreamReceiver;
import io.firebus.utils.StreamSender;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;

import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class S3StreamAdapter extends Adapter implements StreamProvider {
	private Logger logger = Logger.getLogger("io.firebus.adapters");
	protected Regions region;
	protected String bucketName;
	protected String folder;
	protected AmazonS3 s3Client;

	public S3StreamAdapter(DataMap c) {
		super(c);
		bucketName = config.getString("bucket");
		folder = config.getString("folder");
		String regionName = config.getString("region");
		region = Regions.fromName(regionName);
		String accessKey = config.getString("accesskey");
		String secretKey = config.getString("secretkey");
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		s3Client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(region).build();
	}


	public Payload acceptStream(Payload payload, final StreamEndpoint streamEndpoint) throws FunctionErrorException {
		try {
			DataMap request = new DataMap(payload.getString());
			String action = request.getString("action");
			final String fileName = request.getString("filename");
			final String filePath = (folder != null ? folder + "/" : "") + fileName;
			
			if(action.equals("get")) {
				S3Object s3Object = s3Client.getObject(bucketName, filePath);
				final InputStream is = s3Object.getObjectContent();
				new StreamSender(is, streamEndpoint, new StreamSender.CompletionListener() {
					public void completed() {
						try {
							streamEndpoint.close();
							is.close();
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
				final File file = new File(fileName);
				final String mime = payload.metadata.get("mime");
				final FileOutputStream fos = new FileOutputStream(file);
				new StreamReceiver(fos, streamEndpoint, new StreamReceiver.CompletionListener() {
					public void completed() {
						try {
							streamEndpoint.close();
							fos.close();
							PutObjectRequest s3req = new PutObjectRequest(bucketName, filePath, new File(fileName));
					        ObjectMetadata metadata = new ObjectMetadata();
					        if(mime != null) 
						        metadata.setContentType(mime);
					        s3req.setMetadata(metadata);
					        s3Client.putObject(s3req);	
					        file.delete();
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
