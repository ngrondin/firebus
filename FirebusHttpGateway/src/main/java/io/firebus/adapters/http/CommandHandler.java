package io.firebus.adapters.http;

import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataException;
import io.firebus.data.DataMap;
import io.firebus.interfaces.Consumer;
import io.firebus.logging.Logger;

public class CommandHandler extends Handler implements Consumer {
	protected List<SecurityHandler> securityHandlers;
		
	public CommandHandler(HttpGateway gw, Firebus f, DataMap c) {
		super(gw, f, c);
	}
	
	public void setSecuritytHandlers(List<SecurityHandler> sh) {
		securityHandlers = sh;
	}

	public void consume(Payload payload) {
		try {
			DataMap command = payload.getDataMap();
			if(command.containsKey("logoutuser")) {
				String username = command.getString("logoutuser");
				for(SecurityHandler sh: securityHandlers) {
					sh.logoutUser(username);
				}
			}
		} catch (DataException e) {
			Logger.severe("fb.http.consume", e);
		}

	}

}
