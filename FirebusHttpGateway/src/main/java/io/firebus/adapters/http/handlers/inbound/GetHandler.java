package io.firebus.adapters.http.handlers.inbound;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.adapters.http.HttpRequest;
import io.firebus.adapters.http.HttpResponse;
import io.firebus.adapters.http.handlers.ReqRespHandler;
import io.firebus.data.DataMap;

public class GetHandler extends ReqRespHandler 
{
	public GetHandler(Firebus f, DataMap c) 
	{
		super(f, c);
	}

	protected Payload produceFirebusRequest(HttpRequest req) throws Exception
	{
		DataMap fbReq = new DataMap();
		String shortPath = req.getShortPath();
		fbReq.put("get", shortPath);
		for(String name : req.getParameterNames()) {
			fbReq.put(name, req.getParameter(name));
		}
		Payload payload = new Payload(fbReq);
		payload.metadata.put("mime", "application/json");
		return payload;
	}

	protected HttpResponse produceHttpResponse(Payload payload) throws Exception
	{
		return new HttpResponse(200, payload.getBytes());
	}

}
