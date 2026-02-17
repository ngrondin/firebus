package io.firebus.aws;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.logging.Logger;
import io.firebus.utils.StreamSender;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

public class S3DynamicManager implements ServiceProvider {
	protected DataMap config;
	protected Firebus firebus;
	
	public S3DynamicManager(DataMap c, Firebus fb) {
		config = c;
		firebus = fb;
	}

	public Payload service(Payload payload) throws FunctionErrorException {
		try {
			DataMap request = payload.getDataMap();
			String action = request.getString("action");
			String bucketName = request.getString("bucket");
			String regionName = request.getString("region");
			Region region = Region.of(regionName);
			String accessKey = request.getString("accesskey");
			String secretKey = request.getString("secretkey");
			S3Client s3Client = S3Client.builder()
		            .region(region)
		            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
		            .build();
			DataMap respMap = new DataMap();
			
			if(action.equals("list")) {
				ListObjectsRequest req = ListObjectsRequest.builder().bucket(bucketName).build();
				ListObjectsResponse resp = s3Client.listObjects(req);
				List<S3Object> objects = resp.contents();
				DataList respList = new DataList();
				for(S3Object object: objects) {
					respList.add(object.key());
				}
				respMap.put("list", respList);
				
			} else if(action.equals("download")) {
				String key = request.getString("key");
				String filename = request.getString("newfilename");
				if(filename == null)
					filename = key.lastIndexOf("/") > -1 ? key.substring(key.lastIndexOf("/")) : key;
				String fileService = request.getString("fileservice");
				DataMap streamReq = new DataMap("action", "put", "filename", filename);
				
				Payload streamReqPayload = new Payload(streamReq);
				streamReqPayload.metadata.put("mime", "application/json");
				streamReqPayload.metadata.put("session", payload.metadata.get("session"));
				streamReqPayload.metadata.put("token", payload.metadata.get("token"));
				StreamEndpoint sep = firebus.requestStream(fileService, streamReqPayload, 10000);

				GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(key).build();
				InputStream is = s3Client.getObject(getObjectRequest);
				
				StreamSender sender = new StreamSender(is, sep, new StreamSender.CompletionListener() {
					public void error(Throwable error) {
						Logger.severe("fb.aws.s3man.download", error);
					}
					
					public void completed(byte[] bytes) {
						try {
							DataMap filemeta = new DataMap(new ByteArrayInputStream(bytes));
							respMap.put("fileuid", filemeta.getString("fileuid"));
							Logger.info("fb.aws.s3man.download");
						} catch(Exception e) {
							Logger.severe("fb.aws.s3man.download", e);
						}
					}
				});
				sender.sync();
				is.close();
				sep.close();
			}
			return new Payload(respMap);
		} catch(Exception e) {
			throw new FunctionErrorException("Error in S3Manager", e, 400);
		}
	}

	
	public ServiceInformation getServiceInformation() {
		return null;
	}

}
