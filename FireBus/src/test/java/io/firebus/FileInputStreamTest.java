package io.firebus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Random;

import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.StreamInformation;
import io.firebus.interfaces.StreamProvider;
import io.firebus.logging.Level;
import io.firebus.logging.Logger;
import io.firebus.utils.InputStream;
import io.firebus.utils.StreamSender;

public class FileInputStreamTest {

	public static void main(String[] args) {
		byte[] bytes = new byte[1000000];
		Random random = new Random();
		random.nextBytes(bytes);
		final int fastHash = Arrays.hashCode(bytes);

		Firebus firebus = new Firebus();
		firebus.registerStreamProvider("filestream", new StreamProvider() {
			public Payload acceptStream(Payload payload, StreamEndpoint streamEndpoint) throws FunctionErrorException {
				System.out.println("Provider end : Accepting : " + payload.getString());
				try {
					new StreamSender(new ByteArrayInputStream(bytes), streamEndpoint);
				} catch(Exception e) {
					
				}
				return new Payload();
			}

			public StreamInformation getStreamInformation() {
				return null;
			}

			public int getStreamIdleTimeout() {
				return 2000;
			}
		}, 10);
		
		try {

			Logger.setLevel(Level.INFO);
			System.out.println("Stream established");
			Thread.sleep(1000);
			StreamEndpoint streamEndpoint = firebus.requestStream("filestream", new Payload("filestreamrequest"), 2000);
			InputStream is = new InputStream(streamEndpoint);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    int nRead;
		    byte[] data = new byte[1024]; 
		    while ((nRead = is.read(data, 0, data.length)) != -1) {
		    	baos.write(data, 0, nRead);
		    }
		    //is.close();
			byte[] recvbytes = baos.toByteArray();
			int recvtHash = Arrays.hashCode(recvbytes);
			if(recvtHash == fastHash) {
				System.out.println("Success");
			} else {
				System.out.println("Failed transfer");
			}
			Thread.sleep(1000);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		firebus.close();
	}
}
