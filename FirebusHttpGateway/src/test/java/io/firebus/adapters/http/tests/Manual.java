package io.firebus.adapters.http.tests;

import java.io.InputStream;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.adapters.http.HttpGateway;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.information.StreamInformation;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.interfaces.StreamHandler;
import io.firebus.interfaces.StreamProvider;

public class Manual {
	
	public static void main(String[] args) {
		try {
			Firebus firebus = new Firebus();
			firebus.registerServiceProvider("serv01", new ServiceProvider() {
				public Payload service(Payload payload) throws FunctionErrorException {
					try {
						DataMap in = payload.getDataMap();
						return new Payload("<form action=\"\" method=\"POST\" enctype=\"multipart/form-data\">\n"
								+ "    <input type=\"text\" name=\"username\" /><br/>"
								+ "    <input type=\"text\" name=\"password\" /><br/>"
								+ "    <input type=\"file\" name=\"file\" /><br/>"
								+ "    <input type=\"submit\" value=\"Submit\" /><br/>"
								+ "</form>\n");
					} catch(Exception e) {
						return null;
					}
				}

				public ServiceInformation getServiceInformation() {
					return null;
				}
			}, 10);
			firebus.registerServiceProvider("serv02", new ServiceProvider() {
				public Payload service(Payload payload) throws FunctionErrorException {
					try {
						DataMap in = payload.getDataMap();
						return new Payload(in);
					} catch(Exception e) {
						return null;
					}
				}

				public ServiceInformation getServiceInformation() {
					return null;
				}
			}, 10);			
			firebus.registerStreamProvider("ws01", new StreamProvider() {
				public Payload acceptStream(Payload payload, StreamEndpoint streamEndpoint) throws FunctionErrorException {
					streamEndpoint.setHandler(new StreamHandler() {
						public void receiveStreamData(Payload payload, StreamEndpoint streamEndpoint) {
							streamEndpoint.send(new Payload("resp"));
						}

						public void streamClosed(StreamEndpoint streamEndpoint) {
							
						}
						
					});
					return new Payload("ok");
				}

				public int getStreamIdleTimeout() {
					return 60000;
				}

				public StreamInformation getStreamInformation() {
					return null;
				}				
			}, 10);
			InputStream is = Manual.class.getClassLoader().getResourceAsStream("io/firebus/adapters/http/tests/test.json");
			DataMap config = new DataMap(is);
			HttpGateway gw = new HttpGateway(config, firebus);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
