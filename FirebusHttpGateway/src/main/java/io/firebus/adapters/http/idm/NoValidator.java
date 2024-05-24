package io.firebus.adapters.http.idm;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.firebus.Firebus;
import io.firebus.adapters.http.IDMHandler;
import io.firebus.adapters.http.HttpGateway;
import io.firebus.data.DataMap;

public class NoValidator extends IDMHandler {
	protected String loginUrl;
	
	public NoValidator(HttpGateway gw, Firebus f, DataMap c) {
		super(gw, f, c);
		loginUrl = handlerConfig.getString("loginurl");
	}
	

	public String getLoginURL(String originalPath) {
		String publicHost = this.httpGateway.getPublicHost();
		String url = loginUrl + "?redirect=" + publicHost + originalPath;
		return url;
	}

	public String geCodeURL() {
		return "";
	}
	
	public String geRefereshURL(String originalPath) {
		return "";
	}
	
	protected void httpService(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
	}

	public void inboundService(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		
	}

}
