package io.firebus.adapters.http.handlers.inbound;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.adapters.http.HttpRequest;
import io.firebus.adapters.http.HttpResponse;
import io.firebus.adapters.http.decoders.URLEncodedFormDecoder;
import io.firebus.adapters.http.handlers.ReqRespHandler;
import io.firebus.data.DataMap;

public class PostFormHandler extends ReqRespHandler 
{
	public PostFormHandler(Firebus f, DataMap c) 
	{
		super(f, c);
	}

	protected Payload produceFirebusRequest(HttpRequest req) throws Exception
	{
		Payload payload = new Payload(URLEncodedFormDecoder.decode(req.getBodyInputStream()));
		payload.metadata.put("post", req.getShortPath());
		payload.metadata.put("mime", req.getHeaderFirstValue("content-type"));
		return payload;
	}

	protected HttpResponse produceHttpResponse(Payload payload) throws Exception
	{
		return new HttpResponse(200, payload.getBytes());
	}	

}
