package io.firebus.adapters.http.websocket;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.adapters.http.WebsocketHandler;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.utils.DataMap;

public class EchoWebsocketHandler extends WebsocketHandler {


	public EchoWebsocketHandler(DataMap c, Firebus f) {
		super(c, f);
	}

	protected void onOpen(String session, Payload payload) {
		// TODO Auto-generated method stub
		
	}

	protected void onStringMessage(String session, String msg) {
		sendStringMessage(session, msg);		
	}

	protected void onBinaryMessage(String session, byte[] msg) {
		this.sendBinaryMessage(session, msg);
	}

	protected void onClose(String session) {
		// TODO Auto-generated method stub
		
	}

	public Payload service(Payload payload) throws FunctionErrorException {
		// TODO Auto-generated method stub
		return null;
	}

	public ServiceInformation getServiceInformation() {
		// TODO Auto-generated method stub
		return null;
	}



}
