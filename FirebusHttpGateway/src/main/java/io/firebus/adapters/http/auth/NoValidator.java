package io.firebus.adapters.http.auth;

import org.apache.http.impl.client.CloseableHttpClient;

import io.firebus.Firebus;
import io.firebus.adapters.http.HttpRequest;
import io.firebus.adapters.http.HttpResponse;
import io.firebus.adapters.http.handlers.AuthValidationHandler;
import io.firebus.data.DataMap;

public class NoValidator extends AuthValidationHandler {
	protected String loginUrl;
	
	public NoValidator(Firebus f, DataMap c, CloseableHttpClient hc) {
		super(f, c, hc);
		loginUrl = handlerConfig.getString("loginurl");
	}
	

	public String getLoginURL(String originalPath) {
		String url = loginUrl + "?redirect=" + publicHost + originalPath;
		return url;
	}

	protected HttpResponse httpService(HttpRequest req) {
		return new HttpResponse();
	}

}
