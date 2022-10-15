package io.firebus.aws;



import java.util.HashMap;
import java.util.Map;

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
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;


public class SMSAdapter extends Adapter implements ServiceProvider {
	protected Region region;
	protected SnsClient client;
	
	public SMSAdapter(DataMap c) {
		super(c);
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
			String phoneNumber = request.getString("phonenumber");
			String sender = request.getString("senderid");
			Map<String, MessageAttributeValue> attributes = new HashMap<String, MessageAttributeValue>();
			if(sender != null) 
				attributes.put("SenderID", MessageAttributeValue.builder().stringValue(sender).dataType("String").build());
            PublishRequest pubReq = PublishRequest.builder()
                    .message(message)
                    .phoneNumber(phoneNumber)
                    .messageAttributes(attributes)
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
