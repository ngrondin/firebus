package io.firebus.aws;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import io.firebus.Payload;
import io.firebus.adapters.Adapter;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.Consumer;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.logging.Logger;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3Adapter extends Adapter implements ServiceProvider, Consumer {
	protected Region region;
	protected String bucketName;
	protected String folder;
	protected S3Client s3Client;

	public S3Adapter(DataMap c) {
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

	public void consume(Payload payload) {
		try
		{
			String fileName = payload.metadata.get("filename");
			String mime = payload.metadata.get("mime");
			if(fileName != null) {
				String filePath = (folder != null ? folder + "/" : "") + fileName;
				Map<String, String> metadata = new HashMap<String, String>();
				if(mime != null) 
			        metadata.put("content-type", mime);
				PutObjectRequest objectRequest = PutObjectRequest.builder()
		                .bucket(bucketName)
		                .key(filePath)
		                .metadata(metadata)
		                .build();

		        s3Client.putObject(objectRequest, RequestBody.fromBytes(payload.getBytes()));
			}
		}
		catch(Exception e)
		{
			Logger.severe("fb.adapter.aws.s3.consume", e);
		}		
	}

	public Payload service(Payload payload) throws FunctionErrorException {
		String fileName = payload.getString();
		try
		{
			String filePath = (folder != null ? folder + "/" : "") + fileName;
			GetObjectRequest getObjectRequest = GetObjectRequest.builder()
	                .bucket(bucketName)
	                .key(filePath)
	                .build();
			
			InputStream is = s3Client.getObject(getObjectRequest);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int v = -1;
			while((v = is.read()) != -1)
				baos.write(v);
			byte[] bytes = baos.toByteArray();
			Payload response = new Payload(bytes);
			return response;
		}
		catch(Exception e)
		{
			throw new FunctionErrorException(e.getMessage());
		}	
	}

	public ServiceInformation getServiceInformation() {
		return null;
	}

}
