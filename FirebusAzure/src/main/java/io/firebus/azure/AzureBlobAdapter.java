package io.firebus.azure;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.logging.Logger;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

import io.firebus.Payload;
import io.firebus.adapters.Adapter;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.Consumer;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.data.DataMap;

public class AzureBlobAdapter extends Adapter implements ServiceProvider, Consumer
{
	private Logger logger = Logger.getLogger("io.firebus.adapters");
	protected String connectionString;
	protected String containerName;
	protected BlobServiceClient blobServiceClient;
	protected BlobContainerClient blobContainerClient;
	
	public AzureBlobAdapter(DataMap c)
	{
		super(c);
		connectionString = config.getString("connection");
		containerName = config.getString("container");
		blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
		blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
	}

	public void consume(Payload payload)
	{
		try
		{
			String fileName = payload.metadata.get("filename");
			if(fileName != null) {
				BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
				File file = new File(fileName);
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(payload.getBytes());
				fos.close();
				blobClient.uploadFromFile(fileName);
				file.delete();
			}
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
		}
	}

	public Payload service(Payload payload) throws FunctionErrorException
	{
		String fileName = payload.getString();
		try
		{
			HashMap<String, String> metadata = new HashMap<String, String>();
			BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			blobClient.download(baos);
			byte[] bytes = baos.toByteArray();
			Payload response = new Payload(metadata, bytes);
			return response;
		}
		catch(Exception e)
		{
			throw new FunctionErrorException(e.getMessage());
		}
	}

	public ServiceInformation getServiceInformation()
	{
		return null;//new ServiceInformation("text/plain", "", "text/plain", "");
	}

}
