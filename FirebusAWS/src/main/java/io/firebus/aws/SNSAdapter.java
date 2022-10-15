package io.firebus.aws;



import io.firebus.Payload;
import io.firebus.adapters.Adapter;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.ServiceProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;


public class SNSAdapter extends Adapter implements ServiceProvider {
	protected String topic;
	protected Region region;
	protected SnsClient client;
	
	public SNSAdapter(DataMap c) {
		super(c);
		topic = config.getString("topic");
		String regionName = config.getString("region");
		region = Region.of(regionName);
		String accessKey = config.getString("accesskey");
		String secretKey = config.getString("secretkey");
		client = SnsClient.builder()
	            .region(region)
	            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
	            .build();
	}

	public Payload service(Payload payload) throws FunctionErrorException {
		try {
			DataMap request = payload.getDataMap();
			String message = request.getString("message");
			PublishRequest pubReq = PublishRequest.builder()
					.topicArn(topic)
                    .message(message)
                    .build();
            client.publish(pubReq);
			return new Payload(new DataMap("Result", "Ok"));			
		} catch(Exception e) {
			throw new FunctionErrorException(e.getMessage());
		}
	}

	public ServiceInformation getServiceInformation() {
		// TODO Auto-generated method stub
		return null;
	}

}
