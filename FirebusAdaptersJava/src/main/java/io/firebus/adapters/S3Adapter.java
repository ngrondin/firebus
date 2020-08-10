package io.firebus.adapters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.logging.Logger;

import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.Consumer;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.utils.DataMap;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class S3Adapter extends Adapter implements ServiceProvider, Consumer {
	private Logger logger = Logger.getLogger("io.firebus.adapters");
	protected Regions region;
	protected String bucketName;
	protected String folder;
	protected AmazonS3 s3Client;

	public S3Adapter(DataMap c) {
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

	public void consume(Payload payload) {
		try
		{
			String fileName = payload.metadata.get("filename");
			String mime = payload.metadata.get("mime");
			if(fileName != null) {
				File file = new File(fileName);
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(payload.data);
				fos.close();
				
				String filePath = (folder != null ? folder + "/" : "") + fileName;
		        PutObjectRequest request = new PutObjectRequest(bucketName, filePath, new File(fileName));
		        ObjectMetadata metadata = new ObjectMetadata();
		        if(mime != null) 
			        metadata.setContentType(mime);
			    request.setMetadata(metadata);
		        s3Client.putObject(request);						
				
				file.delete();
			}
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
		}		
	}

	public Payload service(Payload payload) throws FunctionErrorException {
		String fileName = new String(payload.data);
		try
		{
			String filePath = (folder != null ? folder + "/" : "") + fileName;
			S3Object s3Object = s3Client.getObject(bucketName, filePath);
			InputStream is = s3Object.getObjectContent();
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
		}	}

	public ServiceInformation getServiceInformation() {
		return null;
	}

}
