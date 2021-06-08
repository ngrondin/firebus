package io.firebus.adapters.http.websocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.adapters.http.HttpGateway;
import io.firebus.adapters.http.WebsocketHandler;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.Consumer;
import io.firebus.utils.DataException;
import io.firebus.utils.DataMap;

public class SignalSubscriberWSHandler extends WebsocketHandler implements Consumer {

	protected Map<String, List<String>> subscriptions;
	
	public SignalSubscriberWSHandler(HttpGateway gw, Firebus f, DataMap c) {
		super(gw, f, c);
		subscriptions = new HashMap<String, List<String>>();
	}

	public ServiceInformation getServiceInformation() {
		return null;
	}

	protected void onOpen(String session, Payload payload) {
		List<String> list = new ArrayList<String>();
		subscriptions.put(session, list);
	}

	protected void onStringMessage(String session, String msg) {
		try {
			DataMap req = new DataMap(msg);
			if(req.containsKey("subscribe")) {
				subscriptions.get(session).add(req.getString("subscribe"));
			} else if(req.containsKey("unsubscribe")) {
				String selector = req.getString("unsubscribe");
				if(selector.equals("*"))
					subscriptions.get(session).clear();
				else
					subscriptions.get(session).remove(selector);
			}
		} catch(DataException e) {
			//TODO: Handle this
		}
	}

	protected void onBinaryMessage(String session, byte[] msg) {
		
	}

	protected void onClose(String session) {
		subscriptions.remove(session);
	}

	public void consume(Payload payload) {
		String signal = payload.getString();
		Iterator<String> it = subscriptions.keySet().iterator();
		while(it.hasNext()) {
			String session = it.next();
			List<String> list = subscriptions.get(session);
			for(int i = 0; i < list.size(); i++)
				if(list.get(i).equals(signal))
					sendStringMessage(session, (new DataMap("signal", signal)).toString());
		}
	}

}
