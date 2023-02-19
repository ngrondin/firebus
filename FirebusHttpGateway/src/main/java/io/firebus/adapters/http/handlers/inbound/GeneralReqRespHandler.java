package io.firebus.adapters.http.handlers.inbound;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.adapters.http.HttpRequest;
import io.firebus.adapters.http.HttpResponse;
import io.firebus.adapters.http.decoders.Decoder;
import io.firebus.adapters.http.handlers.ReqRespHandler;
import io.firebus.data.DataMap;

public class GeneralReqRespHandler extends ReqRespHandler {

	public GeneralReqRespHandler(Firebus f, DataMap c) {
		super(f, c);
	}

	protected Payload produceFirebusRequest(HttpRequest req) throws Exception {
		Payload payload = null;
		if(req.getMethod().equals("GET") || req.getMethod().equals("DELETE")) {
			DataMap fbReq = new DataMap();
			fbReq.put("get", req.getShortPath());
			for(String name : req.getParameterNames()) {
				fbReq.put(name, req.getParameter(name));
			}
			payload = new Payload(fbReq);
		} else {
			Object o = Decoder.decode(req.getBodyInputStream(), req.getHeaderFirstValue("Content-type"));
			if(o instanceof DataMap) {
				payload = new Payload((DataMap)o);
			}
			if(payload != null) {
				payload.metadata.put("post", req.getShortPath());
			}
		}
		return payload;
	}

	protected HttpResponse produceHttpResponse(Payload payload) throws Exception {
		return new HttpResponse(200, payload.getBytes());
	}

}
