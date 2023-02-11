package io.firebus.adapters.http.handlers.inbound;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.adapters.http.HttpRequest;
import io.firebus.adapters.http.HttpResponse;
import io.firebus.adapters.http.handlers.ReqRespHandler;
import io.firebus.data.DataMap;

public class PostJsonHandler extends ReqRespHandler 
{
	public PostJsonHandler(Firebus f, DataMap c) 
	{
		super(f, c);
	}
	
	protected Payload produceFirebusRequest(HttpRequest req) throws Exception
	{
		String shortPath = req.getShortPath();
		DataMap body = new DataMap(req.getBodyInputStream());
		Payload payload = new Payload(body.toString());
		payload.metadata.put("post", shortPath);
		payload.metadata.put("mime", "application/json");
		return payload;
	}

	protected HttpResponse produceHttpResponse(Payload payload) throws Exception
	{
		return new HttpResponse(200, payload.getBytes());
	}	

}
